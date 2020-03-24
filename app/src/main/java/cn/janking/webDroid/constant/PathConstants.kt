package cn.janking.webDroid.constant

import android.os.Environment
import cn.janking.webDroid.util.AppUtils
import cn.janking.webDroid.util.FileUtils
import cn.janking.webDroid.util.Utils
import java.io.File

/**
 * 路径帮助类
 */
object PathConstants {
    const val CONFIG_FILE = "config.json"
    const val CONFIG_DEMO = "template/config.json"
    const val MANIFEST_FILE = "AndroidManifest.xml"
    const val AUTHOR_AVATAR = "img_author_avatar.jpg"
    const val APP_ICON = "ic_launcher.png"
    const val APP_ICON_DEFAULT = "ic_launcher_default.png"
    const val TAB_ICON_PREFIX = "ic_tab_"
    const val TAB_ICON_DEFAULT = "ic_tab_default.png"
    /**
     * key
     */
    const val DEFAULT_KEY_PASSWORD = "123456"
    const val DEFAULT_STORE_PASSWORD = "123456"
    const val DEFAULT_KEY_ALIAS = "webdroid"
    /**
     * 存储数据的主路径
     */
    private val dirRoot: String
        get() = Utils.getApp().getExternalFilesDir(
            null
        ) ?.let {
            FileUtils.getExistDir(it).absolutePath
        } ?: ""

    /**
     * 临时存储目录
     */
    val dirTemp: String
        get() = dirRoot + File.separator + "temp"

    /**
     * webview相关的缓存
     */
    val dirWebCache : String = Utils.getApp().cacheDir.absolutePath + File.separator + "web-cache"

    /**
     * 输出产物的路径
     */
    private val outRoot: String
        get() = Environment.getExternalStorageDirectory().absolutePath + File.separator + AppUtils.getAppName()

    /**
     * 存放apk的路径
     */
    val dirApk: String
        get() = outRoot + File.separator + "apk"

    /**
     * 保存图片的路径
     */
    val dirSaveImage: String
        get() = outRoot + File.separator + "image"

    /**
     * 获取某个存储目录下的子目录
     */
    fun getSubRoot(sub: String): String {
        return dirRoot + File.separator + sub
    }

    /**
     * 获取模板目录下的文件
     */
    fun getSubTemplate(sub: String): String {
        return dirTemplate + File.separator + sub
    }

    /**
     * 获取模板目录
     */
    val dirTemplate: String
        get() = getSubRoot("template")

    /**
     * 获取密钥目录
     */
    private val dirKey: String
        get() = getSubRoot("key")

    /**
     * 获取jks密钥
     */
    val jks: String
        get() = dirKey + File.separator + "webdroid.jks"

    /**
     * 获取临时的apk文件，即未签名的apk
     */
    val fileApkUnsigned: String
        get() = getSubRoot("apk" + File.separator + "unsigned.apk")

    /**
     * 获取生成的apk文件路径，即签名的apk
     */
    fun getFileApkSigned(name: String): String {
        return FileUtils.getExistFile(dirApk + File.separator + name + ".apk")
            .absolutePath
    }

    /**
     * 获取解压的apk目录
     */
    val dirUnzippedApk: String
        get() = getSubRoot("unzippedApk")

    /**
     * 获取解压的apk目录
     */
    fun getSubUnzippedApk(sub: String): String {
        return dirUnzippedApk + File.separator + sub
    }

    /**
     * 获取解压的apk目录下的META-INF目录
     */
    val dirUnzippedApkMetaINF: String
        get() = getSubUnzippedApk("META-INF")

    /**
     * 获取解压的apk目录下的assets目录
     */
    val dirUnzippedApkAssets: String
        get() = getSubUnzippedApk("assets")


    /**
     * 获取解压的apk目录下的assets目录
     */
    val dirUnzippedApkDrawable: String
        get() = getSubUnzippedApk("res" + File.separator + "drawable")

    /**
     * 获取解压的apk目录下的assets目录的子目录或子文件
     */
    fun getSubUnzippedApkAssets(sub: String): String {
        return dirUnzippedApkAssets + File.separator + sub
    }

    /**
     * 获取解压的apk目录下的drawable目录的子目录或子文件
     */
    fun getSubUnzippedApkDrawable(sub: String): String {
        return dirUnzippedApkDrawable + File.separator + sub
    }

}