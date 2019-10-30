package com.impte.wecard.biz.find;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public interface FindChatItemService {
    Map<String, Object> findChatItem(String chatItemId);
}
