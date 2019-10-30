package com.impte.wecard.biz.find;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public interface FindRoomMembersService {
    Map<String, Object> findRoomMembers(String roomId);
}
