package com.impte.wecard.utils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Time {
    public static String TimestampToString(Timestamp timestamp){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//定义格式，不显示毫秒
        String strTime = df.format(timestamp);
        return strTime;
    }

    public static String getNowToString(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//定义格式，不显示毫秒
        Timestamp now = new Timestamp(System.currentTimeMillis());//获取系统当前时间
        String str = df.format(now);
        return str;
    }
}
