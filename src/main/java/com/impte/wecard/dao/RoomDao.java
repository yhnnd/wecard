package com.impte.wecard.dao;

import com.impte.wecard.domain.po.Room;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomDao extends GenericDao<Room, String> {
    Room verifyRoomExist(String id);
    List<Room> findUserRooms(String userId);
    int insertRoom(@Param("id") String id, @Param("roomName") String roomName,
        @Param("ownerId") String ownerId);
    List<Room> findRooms(
        @Param("roomName") String roomName,
        @Param("offset") int offset,
        @Param("limit") int limit
    );
    int updateStatusNotExist(String id);
    int setRoomName(@Param("id") String id, @Param("roomName") String roomName);
    Room findSimpleRoom(String roomId);
}
