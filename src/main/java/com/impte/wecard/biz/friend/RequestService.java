package com.impte.wecard.biz.friend;

public interface RequestService {
    String makeFriendRequest(String verifyName, String groupId, String remark, String requestMsg);
    String joinRoomRequest(String roomId, String requestMsg);
}
