package com.impte.wecard.utils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author justZero
 * @since 2018-3-19
 */
public class DateFormatUtil {

    private static final long ONE_MINUTE = 60 * 1000L;

    /**
     * 根据毫秒时间戳来格式化字符串
     * <p>一分钟前显示刚刚、今天显示今天、昨天显示昨天.
     * 早于昨天的显示具体年-月-日，如2017-06-12；
     * @param timeStamp 毫秒值
     * @return 刚刚 今天 昨天 或者 yyyy-MM-dd HH:mm:ss类型字符串
     */
    public static String format(Timestamp timeStamp, String pattern) {
        long curTimeMillis = System.currentTimeMillis();
        long todayStartMillis = curTimeMillis - timeStamp.getTime();
        long myTime = timeStamp.getTime();
        if (todayStartMillis < ONE_MINUTE) {
            return "刚刚";
        }
        if(myTime >= todayStartMillis) {
            return "今天";
        }
        int oneDayMillis = 24 * 60 * 60 * 1000;
        long yesterdayStartMillis = todayStartMillis - oneDayMillis;
        if(myTime >= yesterdayStartMillis) {
            return "昨天";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return  sdf.format(new Date(myTime));
    }

}
