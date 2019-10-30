package com.impte.wecard.biz.find;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public interface FindRoomService {
    Map<String, Object> findRoom(String roomId);
    Map<String, Object> findSimpleRoom(String roomId);
}
