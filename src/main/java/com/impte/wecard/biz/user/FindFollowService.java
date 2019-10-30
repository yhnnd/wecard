package com.impte.wecard.biz.user;

import java.util.Map;

public interface FindFollowService {
    Map<String, Object> findFollowing();
    Map<String, Object> findFans();
}
