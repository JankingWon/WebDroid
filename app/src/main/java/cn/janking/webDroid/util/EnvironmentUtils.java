package cn.janking.webDroid.util;

import android.os.Environment;

import java.io.File;

/**
 * 环境常量帮助类
 */
public class EnvironmentUtils {
    public static final String DEFAULT_CONFIG_FILE = "config.json";

    /**
     * 存储数据的主路径
     */
    static private String getDirRoot(){
        return Environment.getExternalStorageDirectory() + File.separator + "/WebDroid";
    };

    /**
     * 获取某个存储目录下的子目录
     */
    static public String getDirRootSub(String sub){
        return getDirRoot() + File.separator + sub;
    };

    /**
     * 获取模板目录
     */
    static public String getDirTemplateSub(String sub){
        return getDirTemplate() + File.separator + sub;
    }

    /**
     * 获取模板目录
     */
    static public String getDirTemplate(){
        return getDirRootSub("template");
    }

    /**
     * 获取密钥目录
     */
    static private String getDirKey(){
        return getDirRootSub("key");
    }

    /**
     * 获取pk密钥
     */
    static public String getKeyPk8(){
        return getDirKey() + File.separator + "platform.pk8";
    }

    /**
     * 获取pem密钥
     */
    static public String getKeyPem(){
        return getDirKey() + File.separator + "platform.x509.pem";
    }

    /**
     * 获取生成的apk目录
     */
    static public String getDirApk(){
        return getDirRootSub("apk");
    }

    /**
     * 获取临时的apk文件，即未签名的apk
     */
    static public String getFileApkUnsigned(){
        return getDirRootSub("apk" + File.separator + "unsigned.apk");
    }

    /**
     * 获取生成的apk文件，即签名的apk
     */
    static public String getFileApkSigned(){
        return getDirRootSub("apk" + File.separator + "signed.apk");
    }

    /**
     * 获取解压的apk目录
     */
    static public String getDirUnzippedApk(){
        return getDirRootSub("unzippedApk");
    }

    /**
     * 获取解压的apk目录
     */
    static public String getDirUnzippedApkSub(String sub){
        return getDirUnzippedApk() + File.separator + sub;
    }

    /**
     * 获取解压的apk目录下的META-INF目录
     */
    static public String getDirUnzippedApkMetaINF(){
        return getDirUnzippedApkSub("META-INF");
    }

    /**
     * 获取解压的apk目录下的assets目录
     */
    static public String getDirUnzippedApkAssets(){
        return getDirUnzippedApkSub("assets");
    }

    /**
     * 获取工程目录
     */
    static public String getDirAllProjects(){
        return getDirRootSub("project");
    }

    /**
     * 获取特定的工程
     */
    static public String getDirProject(String projectName){
        return getDirAllProjects() + File.separator + projectName;
    }

    /**
     * 获取工程下的资源目录
     */
    static public String getDirProjectRes(String projectName){
        return getDirProject(projectName) + File.separator + "res";
    }

    /**
     * 获取工程下的manifest
     */
    static public String getFileProjectManifest(String projectName){
        return getDirProject(projectName) + File.separator + "AndroidManifest.xml";
    }

    /**
     * 获取工程下的配置文件
     */
    static public String getFileConfig(String projectName){
        return getDirProject(projectName) + File.separator + "config.properties";
    }
}