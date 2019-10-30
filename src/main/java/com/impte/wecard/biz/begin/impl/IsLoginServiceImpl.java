package com.impte.wecard.biz.begin.impl;

import com.impte.wecard.biz.begin.IsLoginService;
import com.impte.wecard.dao.UserDao;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class IsLoginServiceImpl implements IsLoginService{

    private final UserDao userDao;

    @Override
    public Map<String, Object> isLogin() {
        String message;
        Map<String, Object> map = new HashMap<>();
        HttpSession session = CurrentUtil.getSession();
        User user = (User) session.getAttribute("user");

        if (user != null && user.getId() != null){
            User newUser = userDao.loginGetUser(user.getId());
            message = "true";
            map.put("user", newUser);
            session.setAttribute("user", newUser);
        }else {
            message = "false";
        }
        map.put("message", message);
        return map;
    }
}
