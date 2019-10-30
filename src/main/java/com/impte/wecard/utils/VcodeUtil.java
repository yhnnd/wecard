package com.impte.wecard.utils;

/**
 * 验证码生产工具类
 * 2018年11月15日11:21:29
 * @author xutong34
 */
public class VcodeUtil {

  /**
   * 生产随机数字
   * @param length 6
   * @return 随机数字
   */
  public static String getRandomNumber(int length){
    double random = 0.0;
    double lowBound = 0.1;
    while (random < lowBound){
      random =  Math.random();
    }
    int verCode = (int)(random * Math.pow(10, length));
    return Integer.toString(verCode);
  }
}
