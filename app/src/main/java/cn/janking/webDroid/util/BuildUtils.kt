package cn.janking.webDroid.util

import android.widget.TextView
import cn.janking.binaryXml.util.ManifestUtils
import cn.janking.webDroid.BuildFinishEvent
import cn.janking.webDroid.InitFinish
import cn.janking.webDroid.constant.PermissionConstants
import cn.janking.webDroid.helper.DialogHelper
import cn.janking.webDroid.model.Config
import com.android.signapk.SignApk
import org.greenrobot.eventbus.EventBus
import java.io.File

/**
 * 用于打包apk的工具类
 */
class BuildUtils {
    companion object {
        /**
         * 是否已经准备好打包apk
         */
        var hasInit: Boolean = false
        /**
         * 请求读写文件权限
         */
        fun requestStoragePermission() {
            PermissionUtils.permission(PermissionConstants.STORAGE)
                .rationale { shouldRequest -> DialogHelper.showRationaleDialog(shouldRequest) }
                .callback(object : PermissionUtils.FullCallback {
                    override fun onGranted(permissionsGranted: List<String>) {
                        //获取权限后才进行初始化
                        init()
                        LogUtils.i("请求权限成功！")
                    }

                    override fun onDenied(
                        permissionsDeniedForever: List<String>,
                        permissionsDenied: List<String>
                    ) {
                        LogUtils.i("请求权限失败！")
                        //如果选择了“拒绝后不再询问”，则引导打开权限设置页面
                        if (permissionsDeniedForever.isNotEmpty()) {
                            DialogHelper.showOpenAppSettingDialog()
                            return
                        }
                    }
                })
                .request()
        }

        /**
         * 初始化
         */
        fun init() {
            if (hasInit) return
            ThreadUtils.executeByCached(object : ThreadUtils.SimpleTask<Unit>() {
                override fun doInBackground() {
                    //复制资源
                    copyAssets("template")
                    copyAssets("key")
                    //解压apk，此项如果在debug模式有问题
                    ZipUtils.unzipFile(
                        File(Utils.getApp().packageResourcePath),
                        FileUtils.getExistDir(EnvironmentUtils.dirUnzippedApk)
                    )
                    //删除原有签名
                    FileUtils.deleteFilesInDirWithFilter(
                        EnvironmentUtils.dirUnzippedApkMetaINF
                    ) { pathname ->
                        FileUtils.getFileExtension(pathname).run {
                            equals("MF") || equals("SF") || equals("RSA")
                        }
                    }
                    //删除原有asset
                    FileUtils.deleteFilesInDirWithFilter(
                        EnvironmentUtils.dirUnzippedApkAssets
                    ) { pathname ->
                        pathname.name.run {
                            !equals(EnvironmentUtils.DEFAULT_CONFIG_FILE)
                        }
                    }
                }

                override fun onFail(t: Throwable?) {
                    LogUtils.w("初始化错误")
                    EventBus.getDefault().post(InitFinish(false))
                    t?.printStackTrace()
                }

                override fun onSuccess(result: Unit) {
                    hasInit = true
                    LogUtils.w("初始化完成")
                    EventBus.getDefault().post(InitFinish(true))
                }
            })
        }

        /**
         * 复制资源
         */
        internal fun copyAssets(assetFolder: String) {
            for (name in Utils.getApp().assets.list(assetFolder)!!) {
                FileUtils.copyFileToFile(
                    Utils.getApp().assets.open(assetFolder + File.separator + name),
                    EnvironmentUtils.getSubRoot(assetFolder + File.separator + name)
                )
            }
        }
        /**
         * 生成apk
         * @todo 字节对齐
         */
        fun build(console: TextView) {
            //如果没有初始化成功，则中断
            if (!hasInit) {
                ConsoleUtils.warning(console, "数据未初始化...")
                //重新请求权限，尝试初始化
                requestStoragePermission()
                return
            }
            ThreadUtils.executeByCached(object : ThreadUtils.SimpleTask<Unit>() {
                override fun doInBackground() {
                    ConsoleUtils.info(console, "正在写入配置...")
                    //写入配置
                    FileUtils.writeToFile(
                        Config.generateJson(),
                        EnvironmentUtils.getSubUnzippedApkAssets(EnvironmentUtils.DEFAULT_CONFIG_FILE)
                    )
                    //使用模板中的manifest
                    FileUtils.copyFileToDir(
                        EnvironmentUtils.getSubTemplate(EnvironmentUtils.DEFAULT_MANIFEST_FILE),
                        EnvironmentUtils.dirUnzippedApk
                    )
                    //修改包名 和 APP名称
                    ManifestUtils(
                        EnvironmentUtils.getSubUnzippedApk(EnvironmentUtils.DEFAULT_MANIFEST_FILE),
                        null
                    ).modifyStringAttribute(
                        AppUtils.getAppPackageName(),
                        Config.instance.appPackage
                    ).modifyStringAttribute(
                        AppUtils.getAppName(),
                        Config.instance.appName
                    ).check().exec()
                    //压缩
                    ConsoleUtils.info(console, "正在压缩...")
                    ZipUtils.zipFiles(
                        File(EnvironmentUtils.dirUnzippedApk).listFiles().toList(),
                        FileUtils.getExistFile(EnvironmentUtils.fileApkUnsigned)
                    )
                    ConsoleUtils.info(console, "正在签名...")
                    if (!FileUtils.isFileExists(EnvironmentUtils.keyPem)
                        || !FileUtils.isFileExists(EnvironmentUtils.keyPk8)
                    ) {
                        throw RuntimeException("key is null")
                    }
                    //签名
                    SignApk.main(
                        arrayOf(
                            EnvironmentUtils.keyPem,
                            EnvironmentUtils.keyPk8,
                            EnvironmentUtils.fileApkUnsigned,
                            EnvironmentUtils.fileApkSigned
                        )
                    )
                }

                override fun onCancel() {
                    ConsoleUtils.warning(console,"打包取消！")
                    EventBus.getDefault().post(BuildFinishEvent())
                }

                override fun onFail(t: Throwable?) {
                    ConsoleUtils.error(console, String.format("打包失败！(%s)", t?.message))
                    t?.printStackTrace()
                    EventBus.getDefault().post(BuildFinishEvent())
                }

                override fun onSuccess(result: Unit?) {
                    ConsoleUtils.success(console, "打包完成！")
                    EventBus.getDefault().post(BuildFinishEvent())
                    //立即安装
                    AppUtils.installApp(EnvironmentUtils.fileApkSigned)
                }
            })
        }
    }
}