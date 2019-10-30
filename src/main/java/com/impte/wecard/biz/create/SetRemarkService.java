package com.impte.wecard.biz.create;

public interface SetRemarkService {
    String setRoomRemark(String remark, String roomId);
    String setFriendRemark(String remark, String friendId);
}
