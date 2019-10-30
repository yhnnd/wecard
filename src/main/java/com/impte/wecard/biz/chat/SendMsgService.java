package com.impte.wecard.biz.chat;

import javax.servlet.http.HttpServletRequest;

public interface SendMsgService {
    String sendMsgToFriend(String friendId, String text);
    String sendMsgToRoom(String roomId, String text);
    String sendMsgToChatItem(String chatItemId, String text);
}
