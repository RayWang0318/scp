package com.ray.scp.utils;

import java.security.SecureRandom;

public class RandomUtils {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    private RandomUtils() {
    }

    /**
     * 获取指定长度的随机字节数组
     * @param size
     * @return
     */
    public static byte[] secureRandomBytes(int size) {
        byte[] bytes = new byte[size];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }

    /**
     * 获取随机字符串
     * @return
     */
    public static String secureRandomString(){
        byte[] randomBytes = secureRandomBytes(16); // 128 bits
        return toHex(randomBytes);
    }

    /**
     * 将字节数组转为十六进制字符串
     * @param bytes
     * @return
     */
    public static String toHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

}
