package com.impte.wecard.biz.chat;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public interface LoadMessagesService {
    Map<String, Object> loadMessages(String chatItemId);
    Map<String, Object> loadLimitMessages(String chatItemId, int offset, int limit);
}
