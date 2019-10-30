package com.impte.wecard.biz.user.impl;

import com.impte.wecard.biz.user.LookUserService;
import com.impte.wecard.dao.CardDao;
import com.impte.wecard.dao.UserDao;
import com.impte.wecard.domain.po.User;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LookUserServiceImpl implements LookUserService {

    private final CardDao cardDao;

    private final UserDao userDao;

    @Override
    public Map<String, Object> lookUser(String username) {
        Map<String, Object> map = new HashMap<>();

        if (username == null || username.equals("")){
            map.put("message", "Parameter not valid");
        }else {
            User user = userDao.lookUser(username);
            if (user == null || user.getId() == null){
                map.put("message", "No results");
            }else {
                map.put("message", "look success");
//                List<Card> hotCards = cardDao.findByUserIdOrderHot(user.getId());
//                List<Card> newCards = cardDao.findByUserIdOrderTime(user.getId());
                map.put("user", user);
                map.put("hotCards", null);
                map.put("newCards", null);
            }
        }
        return map;
    }
}
