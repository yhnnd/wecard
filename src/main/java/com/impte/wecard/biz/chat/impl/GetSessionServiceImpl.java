package com.impte.wecard.biz.chat.impl;

import com.impte.wecard.biz.chat.GetSessionService;
import com.impte.wecard.utils.websocket.OnlineSession;
import com.impte.wecard.utils.websocket.WebSocket;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class GetSessionServiceImpl implements GetSessionService {

    @Override
    public OnlineSession getUserSession(String userId) {

        if (userId != null){
            Map<String, OnlineSession> webSockets = WebSocket.getWebSockets();
            OnlineSession onlineSession = webSockets.get(userId);
            if (onlineSession != null){
                return onlineSession;
            }
        }
        return null;
    }

}
