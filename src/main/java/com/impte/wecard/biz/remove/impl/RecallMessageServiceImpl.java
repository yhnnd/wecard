package com.impte.wecard.biz.remove.impl;

import com.impte.wecard.biz.chat.OnlineMsgService;
import com.impte.wecard.biz.remove.RecallMessageService;
import com.impte.wecard.dao.ChatItemDao;
import com.impte.wecard.dao.MessageDao;
import com.impte.wecard.dao.RoomDao;
import com.impte.wecard.dao.RoomMemberDao;
import com.impte.wecard.domain.po.ChatItem;
import com.impte.wecard.domain.po.Message;
import com.impte.wecard.domain.po.Room;
import com.impte.wecard.domain.po.RoomMember;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Service
@AllArgsConstructor
public class RecallMessageServiceImpl implements RecallMessageService {

    private final MessageDao messageDao;
    private final ChatItemDao chatItemDao;
    private final OnlineMsgService onlineMsgService;
    private final RoomMemberDao roomMemberDao;
    private final RoomDao roomDao;
    private final DataSourceTransactionManager txManager;

    @Override
    public String recallFriendMessage(String messageId, String friendId) {
        String result;
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (messageId == null || "".equals(messageId)){
            result = "MessageId cannot be empty";
        }else if (friendId == null || "".equals(friendId)){
            result = "FriendId cannot be empty";
        }else {
            String userId = user.getId();
            //验证消息是否属于当前用户
            Message message = messageDao.verifyMsgBelong(messageId, userId);
            //检查这条消息是否已经被撤回
            int recall = messageDao.findRecall(messageId);
            if (message == null){
                result = "Message does not exist";
            }else if (recall == 1){
                result = "Message has been recalled";
            }else{
                String friendToMeChatItemId = null;

                DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);// 事物隔离级别，开启新事务
                TransactionStatus status = txManager.getTransaction(def);// 获得事务状态
                try{
                    //修改消息recall字段为1，更新撤回时间，抹除其他字段
                    messageDao.setRecall(messageId);
                    //找到自己和好友的好友chatItem
                    ChatItem meToFriendChatItem = chatItemDao.findByObjectId(userId, friendId);
                    //获取好友的对应的聊天栏
                    ChatItem friendToMeChatItem = chatItemDao.findByObjectId(friendId, userId);
                    friendToMeChatItemId = friendToMeChatItem.getId();
                    //更新自己数据库的聊天栏
                    chatItemDao.updateTimeAndMsgId(meToFriendChatItem.getId(), messageId);
                    //更新好友数据库的聊天栏
                    chatItemDao.updateTimeAndMsgId(friendToMeChatItem.getId(), messageId);

                    result =  "Recall success";
                    //事务提交
                    txManager.commit(status);
                }catch(Exception e){
                    CurrentUtil.getSession().removeAttribute("user");
                    result =  "Recall fail";
                    //事务回滚
                    txManager.rollback(status);
                }
                //发送更新消息给自己
                onlineMsgService.sendCommand(
                    userId,
                    "messageId",
                    messageId,
                    "update_message"
                );
                //发送更新消息给好友
                onlineMsgService.sendToSelf(
                    friendId,
                    friendToMeChatItemId,
                    messageId,
                    "update_message"
                );
            }
        }

        return result;
    }

    @Override
    public String recallRoomMessage(String messageId, String roomId) {
        String result;
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (messageId == null || messageId.equals("")){
            result = "MessageId cannot be empty";
        }else if (roomId == null || roomId.equals("")){
            result = "RoomId cannot be empty";
        }else {
            String userId = user.getId();
            //验证消息是否属于当前用户
            Message message = messageDao.verifyMsgBelong(messageId, userId);
            //检查这条消息是否已经被撤回
            int recall = messageDao.findRecall(messageId);
            //验证放假是否存在6
            Room room = roomDao.verifyRoomExist(roomId);
            //验证当前用户是否是
            if (message == null){
                result = "Message does not exist";
            }else if (room == null){
                result = "Room does not exist";
            }else if (recall == 1){
                result = "Message has been recalled";
            }else{
                Map<String, String> memberIdAndChatItemId = new HashMap<>();

                //找到所有群成员
                List<RoomMember> roomMembers = roomMemberDao.findRoomMembers(roomId);
                DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);// 事物隔离级别，开启新事务
                TransactionStatus status = txManager.getTransaction(def);// 获得事务状态
                try{
                    //修改消息recall字段为1，更新撤回时间，抹除其他字段
                    messageDao.setRecall(messageId);
                    if (roomMembers != null && roomMembers.size() != 0){
                        for (RoomMember roomMember: roomMembers){
                            String roomMemberId = roomMember.getId();

                            //找到成员对房间的chatItem
                            ChatItem memToRoomChatItem = chatItemDao.findByObjectId(roomMemberId, roomId);
                            String memToRoomChatItemId = memToRoomChatItem.getId();
                            //更新自己数据库的聊天栏
                            chatItemDao.updateTimeAndMsgId(memToRoomChatItemId, messageId);
                            //存入指令
                            memberIdAndChatItemId.put(roomMemberId, memToRoomChatItemId);
                        }
                    }
                    result =  "Recall success";
                    //事务提交
                    txManager.commit(status);
                }catch(Exception e){
                    result =  "Recall fail";
                    //事务回滚
                    txManager.rollback(status);
                }

                //脱离事务后再发送消息
                for (String memberId: memberIdAndChatItemId.keySet()){
                    //发送消息给所有用户，不插未读表
                    onlineMsgService.sendToSelf(
                        memberId,
                        memberIdAndChatItemId.get(memberId),
                        messageId,
                        "update_message"
                    );
                }
            }
        }

        return result;
    }
}
