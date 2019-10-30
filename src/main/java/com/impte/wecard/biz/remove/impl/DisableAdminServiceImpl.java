package com.impte.wecard.biz.remove.impl;

import com.impte.wecard.biz.remove.DisableAdminService;
import com.impte.wecard.dao.RoomMemberDao;
import com.impte.wecard.domain.po.RoomMember;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DisableAdminServiceImpl implements DisableAdminService {

    private final RoomMemberDao roomMemberDao;

    @Override
    public String disableAdmin(String adminId, String roomId) {
        String message;
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (adminId == null || adminId.equals("")){
            message = "AdminId cannot be empty";
        }else if (roomId == null || roomId.equals("")){
            message = "RoomId cannot be empty";
        }else {
            String userId = user.getId();
            //验证当前用户是否是群主
            RoomMember owner = roomMemberDao.verifyRoomOwner(userId, roomId);
            //验证被处理人是不是管理员
            RoomMember admin = roomMemberDao.verifyRoomAdmin(adminId, roomId);
            if (owner == null){
                message = "You are not owner";
            }else if (admin == null){
                message = "The user is not admin";
            }else {
                int result = roomMemberDao.disableAdmin(admin.getId());
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
