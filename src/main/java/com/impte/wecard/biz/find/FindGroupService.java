package com.impte.wecard.biz.find;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public interface FindGroupService {
    Map<String, Object> findGroup(String groupId);
}
