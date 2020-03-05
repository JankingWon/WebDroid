package cn.janking.webDroid.util

import android.os.Environment
import java.io.File

/**
 * 环境常量帮助类
 */
object EnvironmentUtils {
    const val DEFAULT_CONFIG_FILE = "config.json"
    const val DEFAULT_MANIFEST_FILE = "AndroidManifest.xml"
    /**
     * 存储数据的主路径
     */
    private val dirRoot: String
        get() = Environment.getExternalStorageDirectory().toString() + File.separator + "/WebDroid"

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
     * 获取pk密钥
     */
    val keyPk8: String
        get() = dirKey + File.separator + "platform.pk8"

    /**
     * 获取pem密钥
     */
    val keyPem: String
        get() = dirKey + File.separator + "platform.x509.pem"

    /**
     * 获取生成的apk目录
     */
    val dirApk: String
        get() = getSubRoot("apk")

    /**
     * 获取临时的apk文件，即未签名的apk
     */
    val fileApkUnsigned: String
        get() = getSubRoot("apk" + File.separator + "unsigned.apk")

    /**
     * 获取生成的apk文件，即签名的apk
     */
    val fileApkSigned: String
        get() = getSubRoot("apk" + File.separator + "signed.apk")

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
     * 获取解压的apk目录下的assets目录的子目录或子文件
     */
    fun getSubUnzippedApkAssets(sub: String): String {
        return dirUnzippedApkAssets + File.separator + sub
    }

    /**
     * 获取工程目录
     */
    val dirAllProjects: String
        get() = getSubRoot("project")

    /**
     * 获取特定的工程
     */
    fun getDirProject(projectName: String): String {
        return dirAllProjects + File.separator + projectName
    }

    /**
     * 获取工程下的资源目录
     */
    fun getDirProjectRes(projectName: String): String {
        return getDirProject(projectName) + File.separator + "res"
    }

    /**
     * 获取工程下的manifest
     */
    fun getFileProjectManifest(projectName: String): String {
        return getDirProject(projectName) + File.separator + "AndroidManifest.xml"
    }

    /**
     * 获取工程下的配置文件
     */
    fun getFileConfig(projectName: String): String {
        return getDirProject(projectName) + File.separator + "config.properties"
    }
}