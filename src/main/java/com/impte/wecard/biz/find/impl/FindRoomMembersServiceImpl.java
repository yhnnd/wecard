package com.impte.wecard.biz.find.impl;

import com.impte.wecard.biz.find.FindRoomMembersService;
import com.impte.wecard.dao.RoomMemberDao;
import com.impte.wecard.domain.po.RoomMember;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FindRoomMembersServiceImpl implements FindRoomMembersService {

    private final RoomMemberDao roomMemberDao;

    @Override
    public Map<String, Object> findRoomMembers(String roomId) {
        String message;
        Map<String, Object> map = new HashMap<>();
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (roomId == null || roomId.equals("")){
            message = "RoomId cannot be empty";
        }else {
            String userId = user.getId();
            //验证该用户是否是群成员，只有群成员才能查看群成员组成
            RoomMember roomMember = roomMemberDao.verifyRoomMember(userId, roomId);
            if (roomMember == null){
                message = "You are not member";
            }else {
                List<RoomMember> roomMembers = roomMemberDao.findRoomMembers(roomId);
                if (roomMembers == null){
                    message = "Find fail";
                }else {
                    map.put("myRole", roomMember.getRole());
                    map.put("roomMembers", roomMembers);
                    message = "Find success";
                }
            }
        }
        map.put("message", message);

        return map;
    }
}
