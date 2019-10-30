package com.impte.wecard.biz.begin.impl;

import com.impte.wecard.biz.Constant;
import com.impte.wecard.biz.begin.LoginService;
import com.impte.wecard.dao.LogDao;
import com.impte.wecard.dao.UserDao;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import com.impte.wecard.utils.MD5;
import com.impte.wecard.utils.websocket.OnlineSession;
import com.impte.wecard.utils.websocket.WebSocket;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Service
@AllArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final UserDao userDao;
    private final DataSourceTransactionManager txManager;

    @Override
    public Map<String, Object> loginService(String verifyName, String password) {
        String message;
        Map<String, Object> map = new HashMap<>();
        HttpSession session = CurrentUtil.getSession();
        if (session.getAttribute(Constant.USER) != null){
            message =  "The browser have Login";
        }else if (verifyName == null || verifyName.equals("")){
            message =  "Username cannot be empty";
        }else if (password == null || password.equals("")){
            message = "Password cannot be empty";
        }else {
            //先用邮箱或用户名加上密码MD5的加密串验证数据库中是否有这个user,如果有就返回id
            String userId = userDao.verifyPassword(verifyName, MD5.getMD5(password));

            if (userId == null){
                //没有找到userId表示验证没通过，用户名或者密码错误，将message设置为登陆失败
                message = "Username or password error";
            }else {
                //验证user是否在其他地方登陆
                Map<String, OnlineSession> webSockets = WebSocket.getWebSockets();
                if (webSockets.get(userId) != null){
                    message = "Have login elsewhere";
                }else {
                    message = login(userId, session);
                }
            }
        }
        map.put("message", message);

        return map;
    }

    @Override
    public String login(String userId, HttpSession session) {
        String message;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus status = txManager.getTransaction(def);
        try{
            //1--执行登录前更新的事务
            userDao.sp_affairs_before_login(userId);
            //2--找到User基本信息
            User user = userDao.loginGetUser(userId);
            //3--将user存入session
            session.setAttribute(Constant.USER, user);

            message = "Login success";
            //事务提交
            txManager.commit(status);
        } catch(Exception e){
            session.removeAttribute(Constant.USER);
            message = "Login fail";
            //事务回滚
            txManager.rollback(status);
        }
        return message;
    }
}
