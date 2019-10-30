package com.impte.wecard.dao;

import com.impte.wecard.domain.po.RoomMember;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomMemberDao extends GenericDao<RoomMember, String> {
    RoomMember verifyRoomMember(@Param("userId") String userId, @Param("roomId") String roomId);
    RoomMember verifyRoomAdmin(@Param("userId") String userId, @Param("roomId") String roomId);
    RoomMember verifyRoomOwner(@Param("userId") String userId, @Param("roomId") String roomId);
    List<String> findAdminIdsByRoomId(String roomId);
    int insertRoomMember(
        @Param("id") String id,
        @Param("role") String role,
        @Param("roomRemark") String roomRemark,
        @Param("roomId") String roomId,
        @Param("userId") String userId
    );
    int findOwnerRoomsNum(String userId);
    int setRoomAdmin(String id);
    int disableAdmin(String id);
    List<RoomMember> findRoomMembers(String roomId);
    int deleteByRoomIdAndUserId(@Param("userId") String userId, @Param("roomId") String roomId);
    int deleteAllMembersByRoomId(String roomId);
    int setRoomRemark(@Param("id") String id, @Param("remark") String remark);
}
