package cn.janking.webDroid.util;

import android.content.Context;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 用ConcurrentMap来缓存属性文件的key-value
 */
public class PropertiesUtil {
    private static Map<String, Properties> loaderMap = new HashMap<String, Properties>();
    private static ConcurrentMap<String, String> configMap = new ConcurrentHashMap<String, String>();
    private static final String DEFAULT_CONFIG_FILE = "template/config.properties";   //eg:data.property

    private static Properties prop = null;

    public static Properties loadProperties(Context context, String fileName) {
        Properties properties = loaderMap.get(fileName);
        if (properties != null) {
            return properties;
        }

        InputStream inputStream = null;
        try {
            inputStream = context.getResources().getAssets().open(fileName);
            if (inputStream == null) {
                throw new FileNotFoundException(fileName);
            }
            properties = new Properties();
            properties.load(inputStream);

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            closeStream(inputStream);
        }
        return properties;
    }

    private static void closeStream(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getStringByKey(Context context, String key, String propName) {
        prop = loadProperties(context, propName);
        if(prop == null){
            return null;
        }
        key = key.trim();
        if (!configMap.containsKey(key)) {
            if (prop.getProperty(key) != null) {
                configMap.put(key, prop.getProperty(key));
            }
        }
        return configMap.get(key);
    }

    public static String getStringByKey(Context context, String key) {
        return getStringByKey(context, key, DEFAULT_CONFIG_FILE);
    }
}