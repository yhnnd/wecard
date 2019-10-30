package com.impte.wecard.biz.chat.impl;

import com.impte.wecard.biz.chat.OnlineMsgService;
import com.impte.wecard.biz.chat.SendMsgService;
import com.impte.wecard.dao.ChatItemDao;
import com.impte.wecard.dao.FriendDao;
import com.impte.wecard.dao.MessageDao;
import com.impte.wecard.dao.RoomMemberDao;
import com.impte.wecard.dao.UserDao;
import com.impte.wecard.domain.po.ChatItem;
import com.impte.wecard.domain.po.RoomMember;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import com.impte.wecard.utils.UUID;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Service
@AllArgsConstructor
public class SendMsgServiceImpl implements SendMsgService {

    private final UserDao userDao;

    private final MessageDao messageDao;

    private final FriendDao friendDao;

    private final RoomMemberDao roomMemberDao;

    private final ChatItemDao chatItemDao;

    private final DataSourceTransactionManager txManager;

    private final OnlineMsgService onlineMsgService;

    @Override
    public String sendMsgToFriend(String friendId, String text) {
        String result;

        User sender = CurrentUtil.getUser();
        if (sender == null || sender.getId() == null){
            result = "Please login";
        }else if (friendId == null || friendId.equals("")){
            result = "FriendId cannot be empty";
        }else if (text == null || text.equals("")){
            result = "Text cannot be empty";
        }else {
            //验证对方是否是自己的好友
            String friendRoomId = friendDao.findFriendsRoomId(sender.getId(), friendId);
            if (friendRoomId == null){
                result = "He/she is not your friend";
            }else {
                //进入事务之前定义数据
                String messageId = UUID.getUUID();
                String meToFriendChatItemId = null;
                String friendToMeChatItemId = null;

                //把消息插入数据库
                DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);// 事物隔离级别，开启新事务
                TransactionStatus status = txManager.getTransaction(def);// 获得事务状态
                try{
                    //插入message
                    messageDao.insertFriendBasicMsg(
                            messageId,
                            text,
                            null,
                            "text",
                            sender.getId(),
                            friendRoomId
                    );
                    //获取我自己的对应的聊天栏
                    ChatItem meToFriendChatItem = chatItemDao.findByObjectId(sender.getId(), friendId);
                    meToFriendChatItemId = meToFriendChatItem.getId();
                    //获取好友的对应的聊天栏
                    ChatItem friendToMeChatItem = chatItemDao.findByObjectId(friendId, sender.getId());
                    friendToMeChatItemId = friendToMeChatItem.getId();
                    //更新自己数据库的聊天栏
                    chatItemDao.updateTimeAndMsgId(meToFriendChatItemId, messageId);
                    //更新好友数据库的聊天栏
                    chatItemDao.updateTimeAndMsgId(friendToMeChatItemId, messageId);

                    result = "Send success";
                    //事务提交
                    txManager.commit(status);
                }catch(Exception e){
                    result = "Send fail";
                    //事务回滚
                    txManager.rollback(status);
                }

                //发送消息给好友
                onlineMsgService.sendToFriend(
                    friendId,
                    friendToMeChatItemId,
                    messageId,
                    "load_new_message"
                );
                //发送消息给自己
                onlineMsgService.sendToSelf(
                    sender.getId(),
                    meToFriendChatItemId,
                    messageId,
                    "load_new_message"
                );
            }
        }

        return result;
    }

    @Override
    public String sendMsgToRoom(String roomId, String text) {
        String result;

        User sender = CurrentUtil.getUser();
        if (sender == null || sender.getId() == null){
            result = "Please login";
        }else if (roomId == null || roomId.equals("")){
            result = "RoomId cannot be empty";
        }else if (text == null){
            result = "Text cannot be empty";
        }else {
            //验证自己是否在这个群里
            RoomMember roomMember = roomMemberDao.verifyRoomMember(sender.getId(), roomId);
            if (roomMember == null){
                result = "You are not member";
            }else {
                List<User> members = userDao.findRoomMembers(roomId);
                if (members == null || members.size() == 0){
                    result = "Send fail";
                }else {
                    //预先定义数据
                    String messageId = UUID.getUUID();
                    Map<String, String> memberIdAndChatItemId = new HashMap<>();

                    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);// 事物隔离级别，开启新事务
                    TransactionStatus status = txManager.getTransaction(def);// 获得事务状态
                    try{
                        messageDao.insertRoomBasicMsg(
                                messageId,
                                text,
                                null,
                                "text",
                                sender.getId(),
                                roomId
                        );
                        //获取我自己的对应的聊天栏
                        ChatItem meToRoomChatItem = chatItemDao.findByObjectId(sender.getId(), roomId);
                        //更新自己数据库的聊天栏
                        chatItemDao.updateTimeAndMsgId(meToRoomChatItem.getId(), messageId);
                        for (User member: members){
                            //获取成员的对应的聊天栏
                            ChatItem memberToRoomChatItem = chatItemDao.findByObjectId(member.getId(), roomId);
                            //更新好友数据库的聊天栏
                            chatItemDao.updateTimeAndMsgId(memberToRoomChatItem.getId(), messageId);
                            memberIdAndChatItemId.put(member.getId(), memberToRoomChatItem.getId());
                        }

                        //设置result为发送成功
                        result = "Send success";
                        //事务提交
                        txManager.commit(status);
                    }catch(Exception e) {
                        result = "Send fail";
                        //事务回滚
                        txManager.rollback(status);
                    }

                    //脱离事务后再发送消息
                    for (String memberId: memberIdAndChatItemId.keySet()){
                        //发送消息给单个群成员
                        onlineMsgService.sendToFriend(
                            memberId,
                            memberIdAndChatItemId.get(memberId),
                            messageId,
                            "load_new_message"
                        );
                    }
                }
            }
        }

        return result;
    }

    @Override
    public String sendMsgToChatItem(String chatItemId, String text) {
        String result;

        User sender = CurrentUtil.getUser();
        if (sender == null || sender.getId() == null){
            result = "Please login";
        }else if (chatItemId == null || chatItemId.equals("")){
            result = "ChatItemId cannot be empty";
        }else if (text == null || text.equals("")){
            result = "Text cannot be empty";
        }else {
            //验证chatItem是否属于此人
            ChatItem chatItem = chatItemDao.verifyChatItem(chatItemId);
            if (chatItem == null || !chatItem.getUser().getId().equals(sender.getId())){
                result = "ChatItem does not exist";
            }else if (chatItem.getType().equals("friend")){
                result = sendMsgToFriend(chatItem.getFriend().getId(), text);
            }else if (chatItem.getType().equals("room")){
                result = sendMsgToRoom(chatItem.getRoom().getId(), text);
            }else {
                result = "ChatItem type error";
            }
        }

        return result;
    }
}
