package com.impte.wecard.biz.chat;

import org.springframework.web.socket.WebSocketSession;

public interface CommandService {
    void sendCommand(String commandType, String key, String value,
        WebSocketSession webSocketSession);
}
