package com.impte.wecard.biz.chat.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.impte.wecard.biz.chat.CommandService;
import java.io.IOException;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Service
public class CommandServiceImpl implements CommandService {

    @Override
    public void sendCommand(String commandType, String key, String value, WebSocketSession webSocketSession){
        Gson gson = new Gson();
        JsonObject message = new JsonObject();
        message.addProperty("type", commandType);
        message.addProperty(key, value);
        //生成消息然后发送
        TextMessage textMsg = new TextMessage(gson.toJson(message));

        try {
            webSocketSession.sendMessage(textMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
