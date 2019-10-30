package com.impte.wecard.dao;

import com.impte.wecard.domain.po.Friend;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendDao extends GenericDao<Friend, String> {
    String findFriendsRoomId(@Param("userId") String userId, @Param("friendId") String friendId);
    int insertFriendItem(
        @Param("id") String id,
        @Param("remark") String remark,
        @Param("groupId") String groupId,
        @Param("friendId") String friendId,
        @Param("userId") String userId
    );
    int insertFriendRoom(
        @Param("id") String id,
        @Param("friendItemOneId") String friendItemOneId,
        @Param("friendItemTwoId") String friendItemTwoId
    );
    int deleteFriendRoom(String friendRoomId);
    Friend findFriend(
        @Param("userId") String userId,
        @Param("friendId") String friendId
    );
    int setFriendRemark(
        @Param("remark") String remark,
        @Param("userId") String userId,
        @Param("friendId") String friendId
    );
    int updateGroupId(
        @Param("groupId") String groupId,
        @Param("userId") String userId,
        @Param("friendId") String friendId
    );
}
