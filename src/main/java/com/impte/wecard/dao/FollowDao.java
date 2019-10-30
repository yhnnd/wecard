package com.impte.wecard.dao;

import com.impte.wecard.domain.po.User;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowDao {
    //插入关注
    int follow(
        @Param("id") String id,
        @Param("userId") String userId,
        @Param("followingId") String followingId
    );
    int unFollow(String followId);
    //验证是否已经关注
    String verifyHasFollowed(
        @Param("userId") String userId,
        @Param("followingId") String followingId
    );

    List<User> findFollowing(String userId);
    List<User> findFans(String userId);
}
