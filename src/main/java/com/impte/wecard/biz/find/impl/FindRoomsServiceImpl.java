package com.impte.wecard.biz.find.impl;

import com.impte.wecard.biz.find.FindRoomsService;
import com.impte.wecard.dao.RoomDao;
import com.impte.wecard.domain.po.Room;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FindRoomsServiceImpl implements FindRoomsService{

    private final RoomDao roomDao;

    @Override
    public Map<String, Object> findRooms(String roomName) {
        return findRoomsLimit(roomName,0,8);
    }

    @Override
    public Map<String, Object> findRoomsLimit(String roomName, int offset, int limit) {
        String message;
        Map<String, Object> map = new HashMap<>();

        // 已经过登录状态检查拦截器鉴权
        if (roomName == null || roomName.equals("")){
            message = "RoomName cannot be empty";
        }else {
            List<Room> rooms = roomDao.findRooms(roomName, offset, limit);
            if (rooms == null){
                message = "Find fail";
            }else {
                map.put("rooms", rooms);
                message = "Find success";
            }
        }
        map.put("message", message);

        return map;
    }
}
