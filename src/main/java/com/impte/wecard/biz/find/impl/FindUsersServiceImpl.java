package com.impte.wecard.biz.find.impl;

import com.impte.wecard.biz.find.FindUsersService;
import com.impte.wecard.dao.UserDao;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FindUsersServiceImpl implements FindUsersService {

    private final UserDao userDao;

    @Override
    public Map<String, Object> findUsers(String username) {
        return findUsersLimit(username, 0, 10);
    }

    @Override
    public Map<String, Object> findUsersLimit(String username, int offset, int limit) {
        String message;
        Map<String, Object> map = new HashMap<>();

        // 已经过登录状态检查拦截器鉴权

        if (username == null || username.equals("")){
            message = "Username cannot be empty";
        }else {
            List<User> users = userDao.findUsersByUsername(username, offset, limit);
            if (users == null){
                message = "Find fail";
            }else {
                map.put("users", users);
                message = "Find success";
            }
        }
        map.put("message", message);

        return map;
    }

    @Override
    public Map<String, Object> findSimpleUser(String userId) {
        String message;
        Map<String, Object> map = new HashMap<>();
        if ("".equals(userId) || userId.length() < 10) {
            message = "Parameter illegal";
        }else {
            User user = userDao.findSimpleUser(userId);
            if (user == null){
                message = "User does not exist";
            }else {
                map.put("user", user);
                message = "Find success";
            }
        }
        map.put("message", message);
        return map;
    }
}
