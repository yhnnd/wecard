package com.impte.wecard.controller;

import com.impte.wecard.biz.qq.QQConnectService;
import com.impte.wecard.utils.CurrentUtil;
import com.qq.connect.QQConnectException;
import com.qq.connect.oauth.Oauth;
import java.io.IOException;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

@CrossOrigin
@Controller
@AllArgsConstructor
public class IndexController {

    private final QQConnectService qqConnectService;

    @GetMapping("/qqLogin")
    public void qqLogin(HttpServletResponse response) throws IOException {
        try {
            response.sendRedirect(new Oauth().getAuthorizeURL(
                Objects.requireNonNull(CurrentUtil.getRequest())));
        } catch (QQConnectException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/qqConnect")
    public String qqConnect(HttpServletRequest request){
        qqConnectService.QQConnect(request);
        return "index";
    }
}
