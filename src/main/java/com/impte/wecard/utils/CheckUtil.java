package com.impte.wecard.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据校验工具类
 * @author justZero
 * @since 2018/1/28
 */
public class CheckUtil {

    /**
     * 校验用户名是否合法<br/>
     * 用户名格式要求：字母数字下划线或汉字组成，长度4~6
     * @param username 待用户名
     * @return 用户名合法返回true；否则返回false
     */
	public static boolean isUsernameLegal(String username) {
		String reStr = "^[\\w\\u4e00-\\u9fa5]{4,6}$";
		return check(reStr, username);
	}

    /**
     * 校验邮箱地址是否合法
     * @param email 待校验邮箱
     * @return 邮箱合法返回true;否则返回false
     */
    public static boolean isEmailLegal(String email) {
        String reStr = "^([a-z0-9A-Z]+)" +
                "@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        return check(reStr, email);
    }

    /**
     * 校验手机号是否合法
     * @param phoneNum 待校验手机号
     * @return 手机号合法返回true;否则返回false
     */
    public static boolean isPhoneNumLegal(String phoneNum) {
        String reStr = "^(((13[0-9])|(15([0-3]|[5-9]))|(18[0,5-9]))\\d{8})|" +
                "(0\\d{2}-\\d{8})|(0\\d{3}-\\d{7})$";
        return check(reStr, phoneNum);
    }

    /**
     * 仅允许汉字
     * @return
     */
    public static boolean isCHTxtLegal(String text) {
        String reStr = "^[\\u4e00-\\u9fa5]+$";
        return check(reStr, text);
    }

    /**
     * 允许汉字、数字和字母
     * @param text
     * @return
     */
    public static boolean isTextLegal(String text) {
        String reStr = "^[a-zA-Z0-9\\u4e00-\\u9fa5]*$";
        return check(reStr, text);
    }

    /**
     * 允许汉字、数字、字母和标点符号
     * 不能为空字符
     * @param text
     * @return
     */
    public static boolean isTextLegal2(String text) {
        String reStr = "^[\\p{P}a-zA-Z0-9\\u4e00-\\u9fa5]+$";
        return check(reStr, text);
    }

    /**
     * 正则校验
     * @param reStr 正则表达式字符串
     * @param data  待匹配数据字符串
     * @return 匹配成功返回true；否则返回false
     */
	private static boolean check(String reStr, String data) {
		try {
            Pattern regex = Pattern.compile(reStr);
            Matcher matcher = regex.matcher(data);
            return matcher.matches();
        } catch(Exception e) {
            return false;
        }
	}

    /**
     * 工具类构造方法私有化：不允许创建实例
     */
    private CheckUtil() {}
}
