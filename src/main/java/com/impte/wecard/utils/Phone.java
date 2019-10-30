package com.impte.wecard.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Phone {
    public static boolean checkMobileNumber(String mobileNumber){
        boolean flag;
        try{
            Pattern regex = Pattern.compile("^(((17[0-9])|(13[0-9])|(15([0-3]|[5-9]))|(18[0,5-9]))\\d{8})|(0\\d{2}-\\d{8})|(0\\d{3}-\\d{7})$");
            Matcher matcher = regex.matcher(mobileNumber);
            flag = matcher.matches();
        }catch(Exception e){
            flag = false;
        }
        return flag;
    }
}

