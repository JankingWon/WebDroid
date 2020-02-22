package cn.janking.webDroid.util.Sign;


import java.io.IOException;

public class Base64Util {
    private static final char[] ALPHABET;

    private static int[] valueDecoding;

    static {
        byte b = 0;
        ALPHABET = new char[]{
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
                'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
                'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
                'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
                'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
                'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', '+', '/'};
        valueDecoding = new int[128];
        for (int i = 0; ; i++) {
            if (i >= valueDecoding.length)
                for (i = b; ; i++) {
                    if (i >= ALPHABET.length)
                        return;
                    valueDecoding[ALPHABET[i]] = i;
                }
            valueDecoding[i] = -1;
        }
        valueDecoding[ALPHABET[i]] = i;
        i++;
        continue;
    }
    }

    public static byte[] decode(String paramString) throws IOException { return decode(paramString, 0, paramString.length()); }

    public static byte[] decode(String paramString, int paramInt1, int paramInt2) throws IOException {
        int j = 0;
        if (paramInt2 % 4 != 0)
            throw new IOException("Base64 string length is not multiple of 4");
        int k = paramInt2 / 4 * 3;
        int i = k;
        if (paramString.charAt(paramInt1 + paramInt2 - 1) == '=') {
            i = --k;
            if (paramString.charAt(paramInt1 + paramInt2 - 2) == '=')
                i = k - 1;
        }
        byte[] arrayOfByte = new byte[i];
        for (i = 0;; i += 4) {
            if (i >= paramInt2)
                return arrayOfByte;
            decodeQuantum(paramString.charAt(paramInt1 + i), paramString.charAt(paramInt1 + i + 1), paramString.charAt(paramInt1 + i + 2), paramString.charAt(paramInt1 + i + 3), arrayOfByte, j);
            j += 3;
        }
    }

    private static void decodeQuantum(char paramChar1, char paramChar2, char paramChar3, char paramChar4, byte[] paramArrayOfbyte, int paramInt) throws IOException {
        int j;
        int i;
        boolean bool = false;
        int k = valueDecoding[paramChar1 & 0x7F];
        int m = valueDecoding[paramChar2 & 0x7F];
        if (paramChar4 == '=') {
            if (paramChar3 == '=') {
                paramChar1 = '\002';
                paramChar2 = Character.MIN_VALUE;
                paramChar3 = bool;
            } else {
                i = valueDecoding[paramChar3 & 0x7F];
                paramChar1 = '\001';
                paramChar3 = bool;
            }
        } else {
            i = valueDecoding[paramChar3 & 0x7F];
            j = valueDecoding[paramChar4 & 0x7F];
            paramChar1 = Character.MIN_VALUE;
        }
        if (k < 0 || m < 0 || i < 0 || j < 0)
            throw new IOException("Invalid character in Base64 string");
        paramArrayOfbyte[paramInt] = (byte)(k << 2 & 0xFC | m >>> 4 & 0x3);
        if (paramChar1 < '\002') {
            paramArrayOfbyte[paramInt + 1] = (byte)(m << 4 & 0xF0 | i >>> 2 & 0xF);
            if (paramChar1 < '\001')
                paramArrayOfbyte[paramInt + 2] = (byte)(i << 6 & 0xC0 | j & 0x3F);
        }
    }

    public static String encode(byte[] paramArrayOfbyte) { return encode(paramArrayOfbyte, 0, paramArrayOfbyte.length); }

    public static String encode(byte[] paramArrayOfbyte, int paramInt1, int paramInt2) {
        int i = 0;
        char[] arrayOfChar = new char[(paramInt2 + 2) / 3 * 4];
        int j = 0;
        while (true) {
            if (i >= arrayOfChar.length)
                return new String(arrayOfChar);
            encodeQuantum(paramArrayOfbyte, paramInt1 + j, paramInt2 - j, arrayOfChar, i);
            j += 3;
            i += 4;
        }
    }

    private static void encodeQuantum(byte[] paramArrayOfbyte, int paramInt1, int paramInt2, char[] paramArrayOfchar, int paramInt3) {
        byte b1 = (byte)0;
        byte b3 = (byte)0;
        byte b2 = (byte)0;
        b1 = paramArrayOfbyte[paramInt1];
        paramArrayOfchar[paramInt3] = ALPHABET[b1 >>> 2 & 0x3F];
        if (paramInt2 > 2) {
            paramInt2 = paramArrayOfbyte[paramInt1 + 1];
            paramInt1 = paramArrayOfbyte[paramInt1 + 2];
            paramArrayOfchar[paramInt3 + 1] = ALPHABET[(b1 << 4 & 0x30) + (paramInt2 >>> 4 & 0xF)];
            paramArrayOfchar[paramInt3 + 2] = ALPHABET[(paramInt2 << 2 & 0x3C) + (paramInt1 >>> 6 & 0x3)];
            paramArrayOfchar[paramInt3 + 3] = ALPHABET[paramInt1 & 0x3F];
            return;
        }
        if (paramInt2 > 1) {
            paramInt1 = paramArrayOfbyte[paramInt1 + 1];
            paramArrayOfchar[paramInt3 + 1] = ALPHABET[(b1 << 4 & 0x30) + (paramInt1 >>> 4 & 0xF)];
            paramArrayOfchar[paramInt3 + 2] = ALPHABET[(paramInt1 << 2 & 0x3C) + (b2 >>> 6 & 0x3)];
            paramArrayOfchar[paramInt3 + 3] = '=';
            return;
        }
        paramArrayOfchar[paramInt3 + 1] = ALPHABET[(b3 >>> 4 & 0xF) + (b1 << 4 & 0x30)];
        paramArrayOfchar[paramInt3 + 2] = '=';
        paramArrayOfchar[paramInt3 + 3] = '=';
    }
}
