package com.impte.wecard.biz.create.impl;

import com.impte.wecard.biz.create.SetRemarkService;
import com.impte.wecard.dao.FriendDao;
import com.impte.wecard.dao.RoomDao;
import com.impte.wecard.dao.RoomMemberDao;
import com.impte.wecard.domain.po.Friend;
import com.impte.wecard.domain.po.Room;
import com.impte.wecard.domain.po.RoomMember;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SetRemarkServiceImpl implements SetRemarkService {

    private final FriendDao friendDao;

    private final RoomDao roomDao;

    private final RoomMemberDao roomMemberDao;

    @Override
    public String setFriendRemark(String remark, String friendId) {
        String message;
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (remark == null || remark.equals("")){
            message = "Remark cannot be empty";
        }else if (friendId == null || friendId.equals("")){
            message = "FriendId cannot be empty";
        }else {
            String userId = user.getId();
            //判断双方是否是好友
            Friend friend = friendDao.findFriend(userId, friendId);
            if (friend == null){
                message = "Friend does not exist";
            }else {
                int result = friendDao.setFriendRemark(remark, userId, friendId);
                if (result == 0){
                    message = "Set fail";
                }else {
                    message = "Set success";
                }
            }
        }
        return message;
    }

    @Override
    public String setRoomRemark(String remark, String roomId) {
        String message;
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (remark == null || remark.equals("")){
            message = "Remark cannot be empty";
        }else if (roomId == null || roomId.equals("")){
            message = "RoomId cannot be empty";
        }else {
            String userId = user.getId();
            //验证房间是否存在
            Room room = roomDao.verifyRoomExist(roomId);
            RoomMember roomMember = roomMemberDao.verifyRoomMember(userId, roomId);
            if (room == null){
                message = "Room does not exist";
            }else if (roomMember == null){
                message = "You are not member";
            }else {
                int result = roomMemberDao.setRoomRemark(roomMember.getId(), remark);
                if (result == 0){
                    message = "Set fail";
                }else {
                    message = "Set success";
                }
            }
        }
        return message;
    }
}
