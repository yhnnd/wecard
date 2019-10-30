package com.impte.wecard.biz.qq;

import com.impte.wecard.domain.po.User;

public interface NewQQUserService {
    User NewQQUser(String userId, String openId, String gender, String nickname, String avatarUrl,
        String city);
}
