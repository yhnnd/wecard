package com.impte.wecard.utils;

import java.security.MessageDigest;
import java.util.Base64;

/**
 * 加密/解密工具类
 *
 * @author justZero
 * @since 2018/1/27
 */
public class Encryptor {

    /**
     * MD5 加密(不可逆-加密密码)
     * @param message 源文
     * @return 加密后的字符串
     */
    public static String md5(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            byte[] passwd = md.digest(message.getBytes());
            return bytesToHexStr(passwd);
        } catch (Exception e) {
            throw new RuntimeException("加密失败...\n"+e.getMessage());
        }
    }

    /**
     * 字节数组转换为16进制字符串<br/>
     * 原理：将byte转换为int，然后利用Integer.toHexString(int)转换成16进制字符串
     * @param bytes
     * @return
     */
    private static String bytesToHexStr(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i< bytes.length; i++) {
            // 由于补码的原因，需要做此运算清除补位
            int val = bytes[i] & 0xff;
            String hexVal = Integer.toHexString(val);
            if (hexVal.length() < 2) {
                sb.append(0);
            }
            sb.append(hexVal);
        }
        return sb.toString();
    }

    /**
     * base64 加密(可逆)
     * @param message 源文
     * @return 加密后的字符串
     */
    public static String base64Encode(String message) {
        try {
            Base64.Encoder encoder = Base64.getEncoder();
            return encoder.encodeToString(message.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("加密失败...\n"+e.getMessage());
        }
    }

    /**
     * base64 解密(可逆)
     * @param message 源文
     * @return 解密后的字符串
     */
    public static String base64Decode(String message) {
        try {
            Base64.Decoder decoder = Base64.getDecoder();
            return new String(decoder.decode(message));
        } catch (Exception e) {
            throw new RuntimeException("解密失败...\n"+e.getMessage());
        }
    }


    /**
     * 工具类构造方法私有化：不允许创建实例
     */
    private Encryptor() {}

}
