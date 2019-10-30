package com.impte.wecard.dao;

import com.impte.wecard.domain.po.Group;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupDao extends GenericDao<Group, String> {
    List<Group> findByUserId(String userId);
    Group verifyGroupExist(@Param("id") String id, @Param("userId") String userId);
    String verifyGroupNameExist(@Param("userId") String userId,
        @Param("groupName") String groupName);
    int insertGroup(@Param("id") String id, @Param("groupName") String groupName,
        @Param("userId") String userId);
    int findGroupFriendNum(String groupId);
    int setGroupName(@Param("id") String id, @Param("groupName") String groupName);
}
