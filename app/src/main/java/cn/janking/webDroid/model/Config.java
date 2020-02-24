package cn.janking.webDroid.model;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

public class Config {
    static private Gson gson = new Gson();
    /**
     * 单例
     */
    static private Config instance = new Config();
    static public Config getInstance(){
        return instance;
    }

    /**
     * 读取配置文件
     */
    static public void read(String configString) throws JsonSyntaxException{
        instance =  gson.fromJson(configString, Config.class);
    }

    /**
     * 配置项
     */
    public String url;
    public boolean debug;
}
