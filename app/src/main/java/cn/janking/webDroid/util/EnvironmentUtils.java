package cn.janking.webDroid.util;

import android.content.Context;
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
     * 获取工程目录
     */
    static public String getDirProjects(){
        return getDirRootSub("project");
    }

    /**
     * 获取生成的apk目录
     */
    static public String getDirApk(){
        return getDirRootSub("apk");
    }

    /**
     * 获取解压的apk目录
     */
    static public String getDirUnzippedApk(){
        return getDirRootSub("unzippedApk");
    }

    /**
     * 获取特定的工程
     */
    static public String getDirWorkProject(String projectName){
        return getDirProjects() + File.separator + projectName;
    }

    /**
     * 获取工程下的资源目录
     */
    static public String getDirProjectRes(String projectName){
        return getDirWorkProject(projectName) + File.separator + "res";
    }

    /**
     * 获取工程下的manifest
     */
    static public String getFileProjectManifest(String projectName){
        return getDirWorkProject(projectName) + File.separator + "AndroidManifest.xml";
    }

    /**
     * 获取工程下的配置文件
     */
    static public String getFileConfig(String projectName){
        return getDirWorkProject(projectName) + File.separator + "config.properties";
    }
}