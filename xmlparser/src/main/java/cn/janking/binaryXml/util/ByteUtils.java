package cn.janking.binaryXml.util;

import java.io.UnsupportedEncodingException;
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

    static public void setEncoding(String code) {
        encoding = code;
    }

    /**
     * 字节序
     * true：大端
     * false：小端
     */
    static private short byteEndian = LITTLE_ENDIAN;

    static public void setEndian(short endian) {
        byteEndian = endian;
    }

    static public String byte2String(byte[] bytes) throws UnsupportedEncodingException {
        if (bytes.length % 2 != 0) {
            return null;
        }
        if (byteEndian == BIG_ENDIAN) {
            return new String(bytes, encoding);
        } else {
            byte[] reverseBytes = new byte[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                if (i % 2 == 0) {
                    reverseBytes[i] = bytes[i + 1];
                } else {
                    reverseBytes[i] = bytes[i - 1];
                }
            }
            return new String(reverseBytes, encoding);
        }
    }

    static public byte[] string2Byte(String s) {
        byte[] bytes = new byte[s.length() * 2];
        if (byteEndian == BIG_ENDIAN) {
            for (int i = 0; i < s.length(); i++) {
                byte[] charBytes = ByteBuffer.allocate(2).putShort((short) s.charAt(i)).array();
                bytes[i * 2] = charBytes[0];
                bytes[i * 2 + 1] = charBytes[1];
            }
        } else {
            for (int i = 0; i < s.length(); i++) {
                byte[] charBytes = ByteBuffer.allocate(2).putShort((short) s.charAt(i)).array();
                bytes[i * 2] = charBytes[1];
                bytes[i * 2 + 1] = charBytes[0];
            }
        }
        return bytes;
    }

    public static int byte2int(byte[] bytes) {
        if (byteEndian == LITTLE_ENDIAN) {
            return (bytes[0] & 0xff) | ((bytes[1] << 8) & 0xff00)
                    | ((bytes[2] << 24) >>> 8) | (bytes[3] << 24);
        } else {
            return (bytes[3] & 0xff) | ((bytes[2] << 8) & 0xff00)
                    | ((bytes[1] << 24) >>> 8) | (bytes[0] << 24);
        }
    }

    public static byte[] int2Byte(int value) {
        byte[] src = new byte[4];
        if (byteEndian == LITTLE_ENDIAN) {
            src[3] = (byte) ((value >> 24) & 0xFF);
            src[2] = (byte) ((value >> 16) & 0xFF);
            src[1] = (byte) ((value >> 8) & 0xFF);
            src[0] = (byte) (value & 0xFF);
        } else {
            src[0] = (byte) ((value >> 24) & 0xFF);
            src[1] = (byte) ((value >> 16) & 0xFF);
            src[2] = (byte) ((value >> 8) & 0xFF);
            src[3] = (byte) (value & 0xFF);
        }
        return src;
    }

    public static byte[] short2Byte(short value) {
        byte[] src = new byte[2];
        if (byteEndian == LITTLE_ENDIAN) {
            src[1] = (byte) ((value >> 8) & 0xFF);
            src[0] = (byte) (value & 0xFF);
        } else {
            src[0] = (byte) ((value >> 8) & 0xFF);
            src[1] = (byte) (value & 0xFF);
        }
        return src;
    }

    /**
     * 使用bytes替换src数组中的[start, start + len)部分
     *
     * @param src 进行操作的源数组
     * @param start 对源数组第几个元素开始替换
     * @param len 替换源数组元素的个数
     * @param bytes 用来替换的新数组，注意，此新数组将完全被替换进去源数组，而不管其长度是否跟{@param len}相等
     * @return 进行替换之后的数组
     */
    public static byte[] replaceBytes(byte[] src, int start, int len, byte[] bytes) {
        if ((start + len) > src.length) {
            return src;
        }
        if(len == bytes.length){
            replaceBytes(src, start, bytes);
            return src;
        }else {
            byte[] newBytes = new byte[src.length + (bytes.length - len)];
            //复制前半部分
            System.arraycopy(src, 0, newBytes, 0, start);
            //复制中间部分
            System.arraycopy(bytes, 0, newBytes, start, bytes.length);
            //复制后半部分
            System.arraycopy(src, start + len, newBytes, start + bytes.length, src.length - start - len);
            return newBytes;
        }
    }

    /**
     * 使用bytes替换src数组中的[start, start + bytes.length())部分
     * @param src 进行操作的源数组
     * @param start 对源数组第几个元素开始替换
     * @param bytes 用来替换的新数组，此新数组将被替代bytes.length()个元素
     */
    public static void replaceBytes(byte[] src, int start, byte[] bytes) {
        if (src == null || bytes == null || start > src.length) {
            return;
        }
        System.arraycopy(bytes, 0, src, start, bytes.length);
    }
}
