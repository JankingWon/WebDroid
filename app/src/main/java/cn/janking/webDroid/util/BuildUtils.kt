package cn.janking.webDroid.util

import android.widget.TextView
import cn.janking.binaryXml.util.ManifestUtils
import cn.janking.webDroid.constant.PermissionConstants
import cn.janking.webDroid.event.BuildFinishEvent
import cn.janking.webDroid.event.CancelBuildEvent
import cn.janking.webDroid.event.InitFinishEvent
import cn.janking.webDroid.helper.DialogHelper
import cn.janking.webDroid.model.Config
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import sun.security.tools.jarsigner.Main
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
         * 输出信息的控件
         */
        var console: TextView? = null

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
                    EventBus.getDefault().post(InitFinishEvent(false))
                    t?.printStackTrace()
                }

                override fun onSuccess(result: Unit) {
                    hasInit = true
                    LogUtils.w("初始化完成")
                    EventBus.getDefault().post(InitFinishEvent(true))
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
        fun build(textView: TextView) {
            //如果没有初始化成功，则中断
            if (!hasInit) {
                ConsoleUtils.warning(console, "数据未初始化...")
                //重新请求权限，尝试初始化
                requestStoragePermission()
                return
            }
            console = textView
            ThreadUtils.executeByCached(object : ThreadUtils.SimpleTask<Unit>() {
                override fun doInBackground() {
                    //设置超时
                    setTimeout(60 * 1000) {
                        onFail(Exception("打包超时"))
                    }
                    EventBus.getDefault().register(this)
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
                    //修改包名 和 APP名称 和 FileProvider
                    ManifestUtils(
                        EnvironmentUtils.getSubUnzippedApk(EnvironmentUtils.DEFAULT_MANIFEST_FILE),
                        null
                    ).modifyStringAttribute(
                        AppUtils.getAppPackageName(),
                        Config.instance.appPackage
                    ).modifyStringAttribute(
                        AppUtils.getAppName(),
                        Config.instance.appName
                    ).modifyStringAttribute(
                        AppUtils.getAppPackageName() + ".DownloadFileProvider",
                        Config.instance.appPackage + ".DownloadFileProvider"
                    ).modifyStringAttribute(
                        AppUtils.getAppPackageName() + ".AgentWebFileProvider",
                        Config.instance.appPackage + ".AgentWebFileProvider"
                    ).check().exec()
                    //压缩
                    ConsoleUtils.info(console, "正在压缩...")
                    ZipUtils.zipFiles(
                        File(EnvironmentUtils.dirUnzippedApk).listFiles().toList(),
                        FileUtils.getExistFile(EnvironmentUtils.fileApkUnsigned)
                    )
                    ConsoleUtils.info(console, "正在签名...")
                    if (!FileUtils.isFileExists(EnvironmentUtils.jks)
                    ) {
                        throw RuntimeException("key is null")
                    }
                    if(isCanceled){
                        return
                    }
                    //v2签名，@todo 反复打包和取消此处会出现Android Fatal Signal 7 (SIGBUS)
/*                    SignApk.main(
                        arrayOf(
                            EnvironmentUtils.keyPem,
                            EnvironmentUtils.keyPk8,
                            EnvironmentUtils.fileApkUnsigned,
                            EnvironmentUtils.fileApkSigned
                        )
                    )*/
                    //v1签名
                    Main.main(
                        arrayOf(
                            "-verbose",
                            "-keystore", EnvironmentUtils.jks,
                            "-storepass", EnvironmentUtils.DEFAULT_STORE_PASSWORD,
                            "-keyPass", EnvironmentUtils.DEFAULT_KEY_PASSWORD,
                            "-signedjar",
                            EnvironmentUtils.fileApkSigned,
                            EnvironmentUtils.fileApkUnsigned,
                            EnvironmentUtils.DEFAULT_KEY_ALIAS
                        )
                    )
                    //删除未签名文件
                    FileUtils.delete(EnvironmentUtils.fileApkUnsigned)
                }

                override fun onCancel() {
                    ConsoleUtils.warning(console, "打包取消！")
                }

                override fun onFail(t: Throwable?) {
                    ConsoleUtils.error(console, String.format("打包失败！(%s)", t?.message))
                    t?.printStackTrace()
                }

                override fun onSuccess(result: Unit?) {
                    ConsoleUtils.success(console, "打包完成！(${EnvironmentUtils.fileApkSigned})")
                    //立即安装
                    AppUtils.installApp(EnvironmentUtils.fileApkSigned)
                }

                override fun onDone() {
                    super.onDone()
                    console = null
                    EventBus.getDefault().post(BuildFinishEvent())
                    EventBus.getDefault().unregister(this)
                }

                @Subscribe
                fun onEvent(cancelBuildEvent: CancelBuildEvent) {
                    cancel()
                }
            })
        }
    }
}