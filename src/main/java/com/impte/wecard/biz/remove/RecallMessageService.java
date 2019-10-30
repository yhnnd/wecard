package com.impte.wecard.biz.remove;

public interface RecallMessageService {
    String recallFriendMessage(String messageId, String friendId);
    String recallRoomMessage(String messageId, String roomId);
}
