package com.impte.wecard.biz.find;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public interface FindRoomsService {
    Map<String, Object> findRooms(String roomName);
    Map<String, Object> findRoomsLimit(String roomName, int offset, int limit);
}
