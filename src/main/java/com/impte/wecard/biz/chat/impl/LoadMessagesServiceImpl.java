package com.impte.wecard.biz.chat.impl;

import com.impte.wecard.biz.chat.LoadMessagesService;
import com.impte.wecard.dao.ChatItemDao;
import com.impte.wecard.dao.FriendDao;
import com.impte.wecard.dao.MessageDao;
import com.impte.wecard.dao.RequestDao;
import com.impte.wecard.dao.SystemNotificationDao;
import com.impte.wecard.domain.po.ChatItem;
import com.impte.wecard.domain.po.Message;
import com.impte.wecard.domain.po.Request;
import com.impte.wecard.domain.po.SystemNotification;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Service
@AllArgsConstructor
public class LoadMessagesServiceImpl implements LoadMessagesService {

    private final ChatItemDao chatItemDao;

    private final FriendDao friendDao;

    private final MessageDao messageDao;

    private final RequestDao requestDao;

    private final SystemNotificationDao systemNotificationDao;

    private final DataSourceTransactionManager txManager;

    @Override
    public Map<String, Object> loadMessages(String chatItemId) {
        return loadLimitMessages(chatItemId, 0, 15);
    }

    @Override
    public Map<String, Object> loadLimitMessages(String chatItemId, int offset, int limit) {
        String message;
        Map<String, Object> map = new HashMap<>();
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (chatItemId == null || chatItemId.equals("")){
            message = "ChatItemId cannot be empty";
        }else {
            //验证chatItem是否存在
            ChatItem chatItem = chatItemDao.verifyChatItem(chatItemId);
            if (chatItem == null || !chatItem.getUser().getId().equals(user.getId())){
                message = "ChatItem does not exist";
            }else {
                String userId = user.getId();
                String type = chatItem.getType();

                DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);// 事物隔离级别，开启新事务
                TransactionStatus status = txManager.getTransaction(def);// 获得事务状态
                try{
                    if (type != null && type.equals("friend")){
                        //提出chatItem中的friendId
                        String friendId = chatItem.getFriend().getId();
                        //找到friendRoom
                        String friendRoomId = friendDao.findFriendsRoomId(userId, friendId);
                        //找到这个friendRoom的所有消息
                        List<Message> chatMessages = messageDao.findMessagesByFriendRoomId(friendRoomId, userId, offset, limit);
                        map.put("messages", chatMessages);
                        map.put("type", type);
                        message = "Load success";
                    }else if (type != null && type.equals("room")){
                        //提出chatItem中的roomId
                        String roomId = chatItem.getRoom().getId();
                        //找到这个room的所有消息
                        List<Message> chatMessages = messageDao.findMessagesByRoomId(roomId, userId, offset, limit);
                        map.put("messages", chatMessages);
                        map.put("type", type);
                        message = "Load success";
                    }else if (type != null && type.equals("inform")){
                        List<Request> requests = requestDao.findUserRequests(userId, offset, limit);
                        map.put("messages", requests);
                        map.put("type", type);
                        message = "Load success";
                    }else if (type != null && type.equals("system")){
                        List<SystemNotification> systemNotifications = systemNotificationDao.findByReceiver(userId, offset, limit);
                        map.put("messages", systemNotifications);
                        map.put("type", type);
                        message = "Load success";
                    }else {
                        message = "Load fail";
                    }
                    //事务提交
                    txManager.commit(status);
                }catch(Exception e) {
                    message = "Send Fail";
                    //事务回滚
                    txManager.rollback(status);
                }

            }
        }
        map.put("message", message);

        return map;
    }
}
