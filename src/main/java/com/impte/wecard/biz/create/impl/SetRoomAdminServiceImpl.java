package com.impte.wecard.biz.create.impl;

import com.impte.wecard.biz.create.SetRoomAdminService;
import com.impte.wecard.dao.RoomDao;
import com.impte.wecard.dao.RoomMemberDao;
import com.impte.wecard.domain.po.Room;
import com.impte.wecard.domain.po.RoomMember;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SetRoomAdminServiceImpl implements SetRoomAdminService {

    private final RoomMemberDao roomMemberDao;

    private final RoomDao roomDao;

    @Override
    public String SetRoomAdmin(String memberId, String roomId) {
        String message;
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (memberId == null || memberId.equals("")){
            message = "MemberId cannot be empty";
        }else {
            //检测房间是否存在
            Room room = roomDao.verifyRoomExist(roomId);
            if (room == null){
                message = "Room does not exist";
            }else if (roomMemberDao.verifyRoomOwner(user.getId(), roomId) == null){
                message = "You are not owner";
            }else {
                //检测改用户是否是房间成员
                RoomMember roomMember = roomMemberDao.verifyRoomMember(memberId, roomId);
                if (roomMember == null){
                    message = "The user is not room member";
                }else if (!roomMember.getRole().equals("member")){
                    message = "The user has been admin";
                }else {
                    int result = roomMemberDao.setRoomAdmin(roomMember.getId());
                    if (result == 0){
                        message = "Set fail";
                    }else {
                        message = "Set success";
                    }
                }
            }
        }
        return message;
    }
}
