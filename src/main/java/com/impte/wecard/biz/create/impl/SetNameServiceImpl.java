package com.impte.wecard.biz.create.impl;

import com.impte.wecard.biz.create.SetNameService;
import com.impte.wecard.dao.GroupDao;
import com.impte.wecard.dao.RoomDao;
import com.impte.wecard.dao.RoomMemberDao;
import com.impte.wecard.domain.po.Group;
import com.impte.wecard.domain.po.RoomMember;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SetNameServiceImpl implements SetNameService {

    private final GroupDao groupDao;

    private final RoomMemberDao roomMemberDao;

    private final RoomDao roomDao;

    @Override
    public String setGroupName(String groupId, String groupName) {
        String message;
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (groupName == null || "".equals(groupName)){
            message = "GroupName cannot be empty";
        }else if (groupName.length() > 24){
            message = "GroupName is too long";
        }else if (groupId == null || "".equals(groupId)){
            message = "GroupId cannot be empty";
        }else {
            String userId = user.getId();
            //验证设个分组是否属于当前用户
            Group group = groupDao.verifyGroupExist(groupId, userId);
            if (group == null){
                message = "Insufficient permissions";
            }else {
                int result = groupDao.setGroupName(groupId, groupName);
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
    public String setRoomName(String roomId, String roomName) {
        String message;
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (roomName == null || roomName.equals("")){
            message = "RoomName cannot be empty";
        }else if (roomName.length() > 24){
            message = "RoomName is too long";
        }else if (roomId == null || roomId.equals("")){
            message = "RoomId cannot be empty";
        }else {
            String userId = user.getId();
            //验证操作者是否是群主
            RoomMember owner = roomMemberDao.verifyRoomOwner(userId, roomId);
            if (owner == null){
                message = "Insufficient permissions";
            }else {
                int result = roomDao.setRoomName(roomId, roomName);
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
