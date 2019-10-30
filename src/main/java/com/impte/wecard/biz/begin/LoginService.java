package com.impte.wecard.biz.begin;

import java.util.Map;
import javax.servlet.http.HttpSession;

public interface LoginService {
    Map<String, Object> loginService(String verifyName, String password);
    String login(String userId, HttpSession session);
}
