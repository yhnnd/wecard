package com.impte.wecard.utils.websocket;

import org.springframework.web.socket.WebSocketSession;

public class OnlineSession {
    private String location;
    private WebSocketSession webSocketSession;

    public OnlineSession() {
    }

    public OnlineSession(String location, WebSocketSession webSocketSession) {
        this.location = location;
        this.webSocketSession = webSocketSession;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public WebSocketSession getWebSocketSession() {
        return webSocketSession;
    }
}
