package com.impte.wecard.dao;

import com.impte.wecard.domain.po.ChatItem;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatItemDao extends GenericDao<ChatItem, String> {
    List<ChatItem> findByUserId(String userId);
    ChatItem findByObjectId(@Param("userId") String userId, @Param("objectId") String objectId);
    ChatItem findInformByUserId(String userId);
    ChatItem findSystemByUserId(String userId);
    ChatItem verifyChatItem(String id);
    int setVisible(@Param("value") int value, @Param("id") String id);

    //更新时间和messageId
    int updateTimeAndMsgId(@Param("id") String id, @Param("messageId") String messageId);
    //更新时间和messageId
    int updateTimeAndRequestId(@Param("id") String id, @Param("requestId") String requestId);
    //更新时间和messageId
    int updateTimeAndSysNotId(@Param("id") String id,
        @Param("systemNotificationId") String systemNotificationId);

    //加好友成功，插入好友chatItem
    int insertFriendChatItem(@Param("id") String id, @Param("friendId") String friendId,
        @Param("userId") String userId);
    //加群成功，插入群chatItem
    int insertRoomChatItem(@Param("id") String id, @Param("roomId") String roomId,
        @Param("userId") String userId);
    //注册成功，插入系统chatItem
    int insertSystemChatItem(@Param("id") String id, @Param("userId") String userId);
    //注册成功，插入请求通知chatItem
    int insertInformChatItem(@Param("id") String id, @Param("userId") String userId);

    //返回一个用户所有的chatItems
    List<ChatItem> findAllUserChatItems(String userId);
    //删除用户双方的chatItems
    int deleteFriendChatItems(@Param("userId") String userId, @Param("friendId") String friendId);
}
