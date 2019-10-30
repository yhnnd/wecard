package com.impte.wecard.biz.chat;

import com.impte.wecard.utils.websocket.OnlineSession;

public interface OnlineMsgService {
    void sendToFriend(String FriendId, String FriendChatItemId, String messageId,
        String commandType);
    void sendToFriend(OnlineSession receiverSession, String FriendChatItemId, String messageId,
        String commandType);
    void sendToSelf(String userId, String userChatItemId, String messageId, String commandType);
    void sendToSelf(OnlineSession mySession, String userChatItemId, String messageId,
        String commandType);
    void sendCommand(String userId, String key, String value, String commandType);
    void sendCommand(OnlineSession receiverSession, String key, String value, String commandType);
}
