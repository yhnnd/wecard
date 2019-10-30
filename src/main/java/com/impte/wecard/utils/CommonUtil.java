package com.impte.wecard.utils;

import java.math.BigInteger;
import java.util.Random;

/**
 * 杂乱功能集一身的工具类
 *
 * @author justZero
 * @since 2018-1-27 15:45:11
 */
public class CommonUtil {

    /**
     * 生成唯一ID标识
     * @return GUID字符串
     */
    public static String genGUID() {
        // toString(36): 36 是将该数值转换为 36 进制。
        return new BigInteger(165, new Random()).toString(36);
    }

    /**
     * 生成随机验证码
     * @param len 需要的验证码位数
     * @return 验证码字符串（字母小写）
     */
    public static String genCaptcha(int len) {
        char[] chars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
                'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
                'u', 'v', 'w', 'x', 'y', 'z' };
        StringBuilder captcha = new StringBuilder();
        Random random = new Random();
        for (int i=0; i < len; i++) {
            captcha.append(chars[random.nextInt(36)]);
        }
        return captcha.toString();
    }

    /**
     * 工具类构造方法私有化：不允许创建实例
     */
    private CommonUtil() {}

}
