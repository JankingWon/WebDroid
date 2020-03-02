package cn.janking.webDroid.util;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;

/**
 * 字节编码转换
 */
public class ByteUtils {
    static private final short LITTLE_ENDIAN = 0;
    static private final short BIG_ENDIAN = 1;
    /**
     * 编码方式
     */
    static private String encoding = "Unicode";
    static public void setEncoding(String code){
        encoding = code;
    }
    /**
     * 字节序
     * true：大端
     * false：小端
     */
    static private short byteEndian = LITTLE_ENDIAN;
    static public void setEndian(short endian){
        byteEndian = endian;
    }

    static public String byte2String(byte[] bytes) throws UnsupportedEncodingException {
        if(bytes.length % 2 != 0){
            return null;
        }
        if(byteEndian == BIG_ENDIAN){
            return new String(bytes, encoding);
        }else {
            byte[] reverseBytes = new byte[bytes.length];
            for(int i = 0; i < bytes.length; i++){
                if(i % 2 == 0){
                    reverseBytes[i] = bytes[i + 1];
                }else {
                    reverseBytes[i] = bytes[i - 1];
                }
            }
            return new String(reverseBytes, encoding);
        }
    }

    static public byte[] string2Byte(String s){
        byte[] bytes = new byte[s.length() * 2];
        if(byteEndian == BIG_ENDIAN){
            for(int i = 0; i < s.length(); i++){
                byte[] charBytes = ByteBuffer.allocate(2).putShort((short) s.charAt(i)).array();
                bytes[i * 2] = charBytes[0];
                bytes[i * 2 + 1] = charBytes[1];
            }
        }else {
            for(int i = 0; i < s.length(); i++){
                byte[] charBytes = ByteBuffer.allocate(2).putShort((short) s.charAt(i)).array();
                bytes[i * 2] = charBytes[1];
                bytes[i * 2 + 1] = charBytes[0];
            }
        }
        return bytes;
    }

}
