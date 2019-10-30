package com.impte.wecard.utils;

import com.impte.wecard.domain.po.User;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 获取当前请求相关类
 *
 * @author xutong
 * @since 2018年10月3日18:20:33
 */
@Slf4j
public class CurrentUtil {

  private static final String CURRENT_USER_ID = "CURRENT_USER_ID";


  public static HttpServletRequest getRequest() {
    try {
      return ((ServletRequestAttributes) Objects
          .requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
    } catch (NullPointerException e){
      log.error("getRequest | HttpServletRequest 获取失败");
      return null;
    }
  }

  public static HttpServletResponse getResponse() {
    try {
      return ((ServletRequestAttributes) Objects
          .requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
    } catch (NullPointerException e){
      log.error("getResponse | HttpServletResponse 获取失败");
      return null;
    }
  }

  private static Object getRequestAttribute(String key) {
    HttpServletRequest request = getRequest();
    if (request != null) {
      return getRequest().getAttribute(key);
    }
    return null;
  }

  public static HttpSession getSession(){
      return Objects.requireNonNull(getRequest()).getSession();
  }

  public static User getUser() {
      return (User) getSession().getAttribute("user");
  }
}

