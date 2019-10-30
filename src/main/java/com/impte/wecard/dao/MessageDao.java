package com.impte.wecard.dao;

import com.impte.wecard.domain.po.Message;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageDao extends GenericDao<Message, String> {
    List<Message> findMessagesByFriendRoomId(
        @Param("friendRoomId") String friendRoomId,
        @Param("userId") String userId,
        @Param("offset") int offset,
        @Param("limit") int limit
    );
    List<Message> findMessagesByRoomId(
        @Param("roomId") String roomId,
        @Param("userId") String userId,
        @Param("offset") int offset,
        @Param("limit") int limit
    );

    Message findFriendMsgItem(
        @Param("id") String id,
        @Param("userId") String userId
    );
    Message findRoomMsgItem(
        @Param("id") String id,
        @Param("userId") String userId
    );

    int insertFriendBasicMsg(
        @Param("id") String id,
        @Param("text") String text,
        @Param("imgUrl") String imgUrl,
        @Param("type") String type,
        @Param("userId") String userId,
        @Param("friendRoomId") String friendRoomId
    );
    int insertRoomBasicMsg(
        @Param("id") String id,
        @Param("text") String text,
        @Param("imgUrl") String imgUrl,
        @Param("type") String type,
        @Param("userId") String userId,
        @Param("roomId") String roomId
    );
    Message verifyFriendMsgBelong(
        @Param("id") String id,
        @Param("userId") String userId
    );
    Message verifyRoomMsgBelong(
        @Param("id") String id,
        @Param("userId") String userId
    );
    Message verifyMsgBelong(
        @Param("id") String id,
        @Param("userId") String userId
    );

    //验证消息是否已经被删除
    String findRemovedMessage(
        @Param("userId") String userId,
        @Param("messageId") String messageId
    );

    int insertRemoveMessage(
        @Param("id") String id,
        @Param("userId") String userId,
        @Param("messageId") String messageId
    );

    int findRecall(String id);
    int setRecall(String id);

    String verifyTypeFriend(String id);
    String verifyTypeRoom(String id);
}
