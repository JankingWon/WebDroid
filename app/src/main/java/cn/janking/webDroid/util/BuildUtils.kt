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
import java.text.SimpleDateFormat
import java.util.*

/**
 * 用于打包apk的工具类
 */
class BuildUtils {
    companion object {
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
            if (!SPUtils.getInstance().getBoolean(ConstUtils.SPKey.hasInit)) {
                ConsoleUtils.warning(console, "数据未初始化...")
                return
            }
            console = textView
            ThreadUtils.executeByCached(object : ThreadUtils.SimpleTask<Unit>() {
                /**
                 * 记录打包开始的时间
                 */
                val startTime = System.currentTimeMillis()
                override fun doInBackground() {
                    //设置超时
                    EventBus.getDefault().register(this)
                    ConsoleUtils.info(console, "正在写入配置...")
                    //写入配置
                    FileUtils.writeToFile(
                        Config.toJsonString(),
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
                    /**
                     * 尽量拦截签名过程
                     */
                    if (isCanceled) {
                        return
                    }
                    //v1签名  @todo 5.0签名会出现No Entry异常
                    Main.main(
                        arrayOf(
                            "-verbose",
                            "-keystore", EnvironmentUtils.jks,
                            "-storepass", EnvironmentUtils.DEFAULT_STORE_PASSWORD,
                            "-keyPass", EnvironmentUtils.DEFAULT_KEY_PASSWORD,
                            "-signedjar",
                            EnvironmentUtils.getFileApkSigned(Config.instance.appName),
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
                    LogUtils.e(t)
                }

                override fun onSuccess(result: Unit?) {
                    val apkPath = EnvironmentUtils.getFileApkSigned(Config.instance.appName)
                    ConsoleUtils.success(console, "打包完成！(${apkPath})")
                    //立即安装
                    AppUtils.installApp(apkPath)
                }

                override fun onDone() {
                    super.onDone()
                    console = null
                    LogUtils.i("本次打包用时 ${SimpleDateFormat("ss", Locale.getDefault()).format(Date(System.currentTimeMillis() - startTime))}s")
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