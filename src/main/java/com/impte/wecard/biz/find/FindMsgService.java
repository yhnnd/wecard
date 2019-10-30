package com.impte.wecard.biz.find;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public interface FindMsgService {
    Map<String, Object> findMessage(String messageId);
}
