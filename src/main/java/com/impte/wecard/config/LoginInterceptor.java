package com.impte.wecard.config;

import com.impte.wecard.domain.po.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 登录鉴权拦截器
 * @author justZero
 * @since 2018/12/26
 */
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            log.debug("Not Login | Ban RequestURL={}", request.getRequestURI());
            response.sendRedirect("/login/unLogin");
            return false;
        }
        log.debug("Logined-Username={}, UserID={} | Pass RequestURL={}",
                user.getUsername(),
                user.getId(),
                request.getRequestURI());
        return true;
    }

}
