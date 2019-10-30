package com.impte.wecard.biz.find.impl;

import com.impte.wecard.biz.find.FindRoomService;
import com.impte.wecard.dao.RoomDao;
import com.impte.wecard.dao.RoomMemberDao;
import com.impte.wecard.domain.po.Room;
import com.impte.wecard.domain.po.RoomMember;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FindRoomServiceImpl implements FindRoomService {

    private final RoomMemberDao roomMemberDao;

    private final RoomDao roomDao;

    @Override
    public Map<String, Object> findRoom(String roomId) {
        String message;
        Map<String, Object> map = new HashMap<>();
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (roomId == null || roomId.equals("")){
            message = "RoomId cannot be empty";
        }else {
            String userId = user.getId();
            //查找群
            Room room = roomDao.findById(roomId);
            //验证当前用户是否是群成员
            RoomMember roomMember = roomMemberDao.verifyRoomMember(userId, roomId);
            if (room == null){
                message = "Room does not exist";
            }else if (roomMember == null) {
                message = "You are not room member";
            }else{
                map.put("room", room);
                message = "Find success";
            }
        }
        map.put("message", message);

        return map;
    }

    @Override
    public Map<String, Object> findSimpleRoom(String roomId) {
        String message;
        Map<String, Object> map = new HashMap<>();

        if ("".equals(roomId) || roomId.length() < 10){
            message = "Parameter illegal";
        }else {
            Room room = roomDao.findSimpleRoom(roomId);
            if (room == null){
                message = "Room does not exist";
            }else{
                map.put("room", room);
                message = "Find success";
            }
        }
        map.put("message", message);

        return map;
    }
}
