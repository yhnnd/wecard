package com.impte.wecard.biz.user.impl;

import com.impte.wecard.biz.user.FindFollowService;
import com.impte.wecard.dao.FollowDao;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FindFollowServiceImpl implements FindFollowService {

    private final FollowDao followDao;

    @Override
    public Map<String, Object> findFollowing() {
        String message;
        Map<String, Object> map = new HashMap<>();
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        String userId = user.getId();
        List<User> following = followDao.findFollowing(userId);
        map.put("following", following);
        message = "Find success";

        map.put("message", message);
        return map;
    }

    @Override
    public Map<String, Object> findFans() {
        String message;
        Map<String, Object> map = new HashMap<>();
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        String userId = user.getId();
        List<User> fans = followDao.findFans(userId);
        map.put("fans", fans);
        message = "Find success";

        map.put("message", message);
        return map;
    }
}
