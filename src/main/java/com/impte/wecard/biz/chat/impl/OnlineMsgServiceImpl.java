package com.impte.wecard.biz.chat.impl;

import com.impte.wecard.biz.chat.CommandService;
import com.impte.wecard.biz.chat.GetSessionService;
import com.impte.wecard.biz.chat.OnlineMsgService;
import com.impte.wecard.dao.UnreadDao;
import com.impte.wecard.domain.po.Unread;
import com.impte.wecard.utils.UUID;
import com.impte.wecard.utils.websocket.OnlineSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Service
@AllArgsConstructor
public class OnlineMsgServiceImpl implements OnlineMsgService {

    private final GetSessionService getSessionService;

    private final UnreadDao unreadDao;

    private final CommandService commandService;

    @Override
    public void sendToFriend(String friendId, String friendChatItemId, String messageId, String commandType) {
        OnlineSession receiverSession = getSessionService.getUserSession(friendId);
        sendToFriend(receiverSession, friendChatItemId, messageId, commandType);
    }

    @Override
    public void sendToFriend(OnlineSession receiverSession, String friendChatItemId, String messageId, String commandType) {
        //把消息插入未读（对于好友的未读）
        String unreadId = UUID.getUUID();
        unreadDao.insertUnread(new Unread(unreadId, messageId, friendChatItemId));

        if (receiverSession != null){
            //如果好友在线则获取他的session
            WebSocketSession receiverWebSession = receiverSession.getWebSocketSession();

            String friendChatLocation = receiverSession.getLocation();//好友的聊天位置
            //------------------------接下来是发送聊天消息---------------------------------------
            //1--第一种情况，好友在线并且在和自己聊天，也就是在聊天栏内
            if (friendChatLocation.equals(friendChatItemId)){
                //好友在和我聊天，所以删除未读
                unreadDao.delete(unreadId);
                //指令1--发送更新聊天栏指令，告诉朋友需要更新的聊天栏，并给出好友的聊天栏Id
                commandService.sendCommand(
                        "update_chatItem",
                        "chatItemId",
                        friendChatLocation,
                        receiverWebSession
                );

                String key = getKey(commandType);
                if (key != null){
                    //指令2--如果对方在和自己聊天，告诉朋友需要更新的消息id,对面通过ajax查询
                    commandService.sendCommand(
                            commandType,
                            key,
                            messageId,
                            receiverWebSession
                    );
                }else {
                    log.error("sendToFriend | 错误指令");
                }
            }
            //2--第二种情况，好友在线，但是并没有和我聊天，只需要发出更新聊天栏指令即可
            else {
                //与上面不同的是，这里不需要删除未读，在对面更新他的聊天栏时，由于好友没有在和自己聊天，未读数量会重新计算
                //（更新未读数量unreadNum是在查询chatItem之前更新的）
                commandService.sendCommand(
                        "update_chatItem",
                        "chatItemId",
                        friendChatItemId,
                        receiverWebSession
                );
            }
        }
    }

    @Override
    public void sendToSelf(String userId, String userChatItemId, String messageId, String commandType) {
        //获得自己的的webSocketSession和httpSession
        OnlineSession mySession = getSessionService.getUserSession(userId);
        sendToSelf(mySession, userChatItemId, messageId, commandType);
    }

    @Override
    public void sendToSelf(OnlineSession mySession, String userChatItemId, String messageId, String commandType) {
        if (mySession != null){
            //获取自己的webSocketSession
            WebSocketSession myWebSession = mySession.getWebSocketSession();

            String myChatLocation = mySession.getLocation();//好友的聊天位置
            if (myChatLocation.equals(userChatItemId)){
                //指令1--发送更新聊天栏指令，告诉自己需要更新的聊天栏，并给出的聊天栏Id
                commandService.sendCommand(
                        "update_chatItem",
                        "chatItemId",
                        myChatLocation,
                        myWebSession
                );

                String key = getKey(commandType);
                if (key != null){
                    //指令2--告诉自己需要更新的消息id,通过ajax查询
                    commandService.sendCommand(
                            commandType,
                            key,
                            messageId,
                            myWebSession
                    );
                }else {
                    System.out.println("错误指令");
                }
            }else {
                commandService.sendCommand(
                        "update_chatItem",
                        "chatItemId",
                        userChatItemId,
                        myWebSession
                );
            }
        }
    }

    @Override
    public void sendCommand(String userId, String key, String value, String commandType) {
        //发送消息和命令给好友
        //获得好友的webSocketSession和httpSession
        OnlineSession receiverSession = getSessionService.getUserSession(userId);
        sendCommand(receiverSession, key, value, commandType);
    }

    @Override
    public void sendCommand(OnlineSession receiverSession, String key, String value, String commandType) {
        //判断是否为空，如果FriendSession为空则好友不在线
        if (receiverSession != null){
            //如果好友在线则获取他的session
            WebSocketSession receiverWebSession = receiverSession.getWebSocketSession();
            commandService.sendCommand(commandType, key, value, receiverWebSession);
        }
    }

    private String getKey(String commandType){
        switch (commandType) {
            case "load_new_request":
            case "update_request":
                return "requestId";
            case "load_new_message":
            case "update_message":
                return "messageId";
            case "load_new_notification":
                return "notificationId";
            default:
                return null;
        }
    }
}
