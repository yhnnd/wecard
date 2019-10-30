package com.impte.wecard.biz.find;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public interface FindUsersService {
    Map<String, Object> findUsers(String username);
    Map<String, Object> findUsersLimit(String username, int offset, int limit);
    Map<String, Object> findSimpleUser(String userId);
}
