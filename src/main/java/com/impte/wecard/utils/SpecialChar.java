package com.impte.wecard.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpecialChar {
    public static boolean usernameContainSpecialChar(String str) {
        String regEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.find();
    }

    public static boolean nicknameContainSpecialChar(String str) {
        String regEx = "[ \\[\\]]|\n|\r|\t";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.find();
    }

    public static boolean ContainSpecialChar(String qString) {
        if (qString!=null) {
            qString = qString.trim();
            String regex = "^[a-zA-Z0-9\u4E00-\u9FA5]+$";
            Pattern pattern = Pattern.compile(regex);
            Matcher match = pattern.matcher(qString);
            boolean b = match.matches();
            if (b) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
}
