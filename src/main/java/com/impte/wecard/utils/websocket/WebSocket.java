package com.impte.wecard.utils.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
public class WebSocket extends TextWebSocketHandler {

    public static final String EXTERNAL = "external";
    public static final String USER_ID = "userId";

    private static Map<String, OnlineSession> webSockets = new ConcurrentHashMap<>();

    private static Map<WebSocketSession, String> webSocketIdMap = new ConcurrentHashMap<>();

    public static Map<String, OnlineSession> getWebSockets() {
        return webSockets;
    }

    public static Map<WebSocketSession, String> getWebSocketIdMap() {
        return webSocketIdMap;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = (String) session.getAttributes().get(USER_ID);
        if (userId != null){
            OnlineSession onlineSession = new OnlineSession(EXTERNAL, session);
            if (webSockets.containsKey(userId)) {
                log.info("afterConnectionEstablished | 重复链接");
                WebSocketSession oldSession = webSockets.get(userId).getWebSocketSession();
                webSocketIdMap.remove(oldSession);
            }
            //1--设置Map
            webSocketIdMap.put(session, userId);
            webSockets.put(userId, onlineSession);
            //3--打印信息
            log.info("新连接加入。。。");
            log.info("当前在线人数：" + webSockets.size());
            log.info("webSocketIdMap.size() = " + webSocketIdMap.size());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus){
        String userId = webSocketIdMap.get(session);
        if (userId != null){
            webSockets.remove(userId);
            webSocketIdMap.remove(session);
            log.info("一个用户连接断开。。。");
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        if(session.isOpen()){
            session.close();
        }
        log.info("连接出错");
        String userId = webSocketIdMap.get(session);
        if (userId != null){
            webSockets.remove(userId);
            webSocketIdMap.remove(session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message){}
    @Override
    public boolean supportsPartialMessages() { return false; }
}