package cn.janking.webDroid.util

import android.widget.TextView
import cn.janking.AXMLTool.util.ManifestUtils
import cn.janking.webDroid.R
import cn.janking.webDroid.constant.PathConstants
import cn.janking.webDroid.event.BuildFinishEvent
import cn.janking.webDroid.event.CancelBuildEvent
import cn.janking.webDroid.model.Config
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import sun.security.tools.jarsigner.Main
import java.io.File

/**
 * 用于打包apk的工具类
 */
object BuildUtils {
    /**
     * 输出信息的控件
     */
    var console: TextView? = null

    /**
     * 生成apk
     * @todo 字节对齐
     */
    fun build(textView: TextView) {
        //如果没有初始化成功，则中断
        if (!SPUtils.getInstance().getBoolean(Utils.getString(R.string.key_has_init))) {
            ConsoleUtils.warning(console, "数据未初始化...")
            return
        }
        console = textView
        ThreadUtils.executeByIo(object : ThreadUtils.SimpleTask<Unit>() {
            /**
             * 记录打包开始的时间
             */
            var startTime = 0L

            override fun doInBackground() {
                EventBus.getDefault().register(this)
                startTime = System.currentTimeMillis()
                ConsoleUtils.info(console, "正在写入配置...")
                //写入配置
                FileUtils.writeToFile(
                    Config.toJsonString(),
                    PathConstants.getSubUnzippedApkAssets(
                        PathConstants.CONFIG_FILE
                    )
                )
                //使用模板中的manifest
                FileUtils.copyFileToDir(
                    PathConstants.getSubTemplate(
                        PathConstants.MANIFEST_FILE
                    ),
                    PathConstants.dirUnzippedApk
                )
                //写入默认 app icon
                FileUtils.copyFileToFile(
                    PathConstants.getSubUnzippedApkDrawable(
                        PathConstants.APP_ICON_DEFAULT
                    ),
                    PathConstants.getSubUnzippedApkDrawable(
                        PathConstants.APP_ICON
                    )
                )
                //写入 app icon
                FileUtils.copyFileToFile(
                    Config.instance.appIcon,
                    PathConstants.getSubUnzippedApkDrawable(
                        PathConstants.APP_ICON
                    )
                )
                //写入 tab icon
                for (i in Config.instance.tabIcons.indices) {
                    //先写入默认tab icon
                    FileUtils.copyFileToFile(
                        PathConstants.TAB_ICON_DEFAULT,
                        PathConstants.getSubUnzippedApkDrawable(
                            "${PathConstants.TAB_ICON_PREFIX}${i}.png"
                        )
                    )
                    FileUtils.copyFileToFile(
                        Config.instance.tabIcons[i],
                        PathConstants.getSubUnzippedApkDrawable(
                            "${PathConstants.TAB_ICON_PREFIX}${i}.png"
                        )
                    )
                }
                //删除 author avatar
                FileUtils.delete(PathConstants.getSubUnzippedApkDrawable(PathConstants.AUTHOR_AVATAR))
                //修改包名、APP名称、APP版本、FileProvider
                ManifestUtils(
                    PathConstants.getSubUnzippedApk(
                        PathConstants.MANIFEST_FILE
                    ),
                    null
                ).modifyUniqueStringAttribute(
                    AppUtils.getAppPackageName(),
                    Config.instance.appPackage
                ).modifyUniqueStringAttribute(
                    AppUtils.getAppName(),
                    Config.instance.appName
                ).modifyUniqueStringAttribute(
                    AppUtils.getAppVersionName(),
                    Config.instance.versionName
                ).modifyVersionCode(
                    1,
                    Config.instance.versionCode
                ).modifyUniqueStringAttribute(
                    AppUtils.getAppPackageName() + ".utilcode.provider",
                    Config.instance.appPackage + ".utilcode.provider"
                ).modifyUniqueStringAttribute(
                    AppUtils.getAppPackageName() + ".DownloadFileProvider",
                    Config.instance.appPackage + ".DownloadFileProvider"
                ).check().exec()
                //压缩
                ConsoleUtils.info(console, "正在压缩...")
                ZipUtils.zipFiles(
                    File(PathConstants.dirUnzippedApk).listFiles().toList(),
                    FileUtils.getExistFile(PathConstants.fileApkUnsigned)
                )
                ConsoleUtils.info(console, "正在签名...")
                if (!FileUtils.isFilePathExists(PathConstants.jks)
                ) {
                    throw RuntimeException("签名秘钥不存在")
                }
                /**
                 * 尽量拦截签名过程
                 */
                if (isCanceled) {
                    return
                }
                //v1签名
                Main.main(
                    arrayOf(
                        "-keystore", PathConstants.jks,
                        "-storepass", PathConstants.DEFAULT_STORE_PASSWORD,
                        "-keyPass", PathConstants.DEFAULT_KEY_PASSWORD,
                        "-signedjar",
                        PathConstants.getFileApkSigned(Config.instance.appName),
                        PathConstants.fileApkUnsigned,
                        PathConstants.DEFAULT_KEY_ALIAS
                    )
                )
                //删除未签名文件
                FileUtils.delete(PathConstants.fileApkUnsigned)
            }

            override fun onCancel() {
                ConsoleUtils.warning(console, "打包取消！")
            }

            override fun onFail(t: Throwable?) {
                ConsoleUtils.error(
                    console,
                    "打包失败！(${t?.javaClass?.name}: ${t?.message})"
                )
                ConsoleUtils.infoAppend(
                    console,
                    ThrowableUtils.getFullStackTrace(t)
                )
                LogUtils.e(t)
            }

            override fun onSuccess(result: Unit?) {
                val apkPath = PathConstants.getFileApkSigned(Config.instance.appName)
                ConsoleUtils.success(console, "打包完成！点击立即安装\n(${apkPath})")
                LogUtils.i("本次打包用时 ${(System.currentTimeMillis() - startTime).toDouble() / 1000}s")
                //立即安装
                install()
            }

            override fun onDone() {
                super.onDone()
                console = null
                EventBus.getDefault().unregister(this)
                EventBus.getDefault().post(BuildFinishEvent())
            }

            @Subscribe
            fun onEvent(cancelBuildEvent: CancelBuildEvent) {
                cancel()
            }
        })
    }

    fun install() {
        val apkPath = PathConstants.getFileApkSigned(Config.instance.appName)
        AppUtils.installApp(apkPath)
    }
}