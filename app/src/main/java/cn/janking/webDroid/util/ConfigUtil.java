package cn.janking.webDroid.util;

import android.content.Context;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;

public class ConfigUtil {
    static public Context context = null;

    static public String getPathRoot(){
        return Environment.getExternalStorageDirectory() + File.separator + "/WebDroid";
    };
    static public String getPathRoot(String sub){
        return getPathRoot() + File.separator + sub;
    };
    static public String getPathEnvironment(){
        return context.getDir("/.webDroid", Context.MODE_PRIVATE).getAbsolutePath();
    }
    static public String getPathPlugin(){
        return context.getDir("/WebDroid/plugin", Context.MODE_PRIVATE).getAbsolutePath();
    }
    static public String getPathCache(){
        return context.getDir("/WebDroid/cache", Context.MODE_PRIVATE).getAbsolutePath();
    }
    static public String getPathTemplate(){
        return getPathRoot("template");
    }
    static public String getPathRes(){
        return getPathTemplate() + File.separator + "res";
    }
    static public String getPathTool(){
        return getPathRoot("tool");
    }
    static public String getPathGen(){
        return getPathRoot("gen");
    }

    static public String getFileAndroid(){
        return getPathTool() + File.separator + "android.jar";
    }
    static public String getFileAPT(){
        return getPathTool() + File.separator + "apt";
    }
    static public String getFileManifest(){
        return getPathTool() + File.separator + "AndroidManifest.xml";
    }

    static public String getFileConfig(){
        return getPathTemplate() + File.separator + "config.properties";
    }
}