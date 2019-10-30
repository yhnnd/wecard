package com.impte.wecard.biz.begin;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public interface ForgetPwdService {
    Map<String, String> confirmAccount(String account);
    String sendVerCode();
    String verifyVerCode(String verCode);
    String setPassword(String password);
}
