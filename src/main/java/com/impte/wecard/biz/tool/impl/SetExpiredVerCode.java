package com.impte.wecard.biz.tool.impl;

import lombok.AllArgsConstructor;

import java.util.TimerTask;
import javax.servlet.http.HttpSession;

/**
 * 用于设置验证码过期
 */
@AllArgsConstructor
public class SetExpiredVerCode extends TimerTask {

    private HttpSession session;
    private String verCodeName;

    public void run() {
        session.setAttribute(verCodeName,"expired");
    }
}
