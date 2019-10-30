package com.impte.wecard.utils;

import java.util.HashMap;
import java.util.Map;

public class RespUtil {
  public static Map<String, String> packMsg(String message){
    Map<String, String> map = new HashMap<>(1);
    map.put("message", message);
    return map;
  }
}
