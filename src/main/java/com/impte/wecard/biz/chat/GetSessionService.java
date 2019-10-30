package com.impte.wecard.biz.chat;

import com.impte.wecard.utils.websocket.OnlineSession;

public interface GetSessionService {
    OnlineSession getUserSession(String userId);
}
