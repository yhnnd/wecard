package com.impte.wecard.biz.remove.impl;

import com.impte.wecard.biz.chat.GetSessionService;
import com.impte.wecard.biz.chat.OnlineMsgService;
import com.impte.wecard.biz.remove.RemoveFriendService;
import com.impte.wecard.dao.ChatItemDao;
import com.impte.wecard.dao.FriendDao;
import com.impte.wecard.dao.SystemNotificationDao;
import com.impte.wecard.domain.po.Friend;
import com.impte.wecard.domain.po.SystemNotification;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import com.impte.wecard.utils.UUID;
import com.impte.wecard.utils.websocket.OnlineSession;
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
public class RemoveFriendServiceImpl implements RemoveFriendService {

    private final FriendDao friendDao;
    private final ChatItemDao chatItemDao;
    private final SystemNotificationDao systemNotificationDao;
    private final DataSourceTransactionManager txManager;
    private final GetSessionService getSessionService;
    private final OnlineMsgService onlineMsgService;

    @Override
    public String removeFriend(String friendId) {
        String message;
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (friendId == null || friendId.equals("")){
            message = "FriendId cannot be empty";
        }else {
            String userId = user.getId();
            //验证两人是否是好友
            String friendRoomId = friendDao.findFriendsRoomId(userId, friendId);
            if (friendRoomId == null){
                message = "Friend does not exist";
            }else {
                //找到这个好友
                Friend my_friend = friendDao.findFriend(userId, friendId);//我的好友，即对方
                Friend him_friend = friendDao.findFriend(friendId, userId);//他的好友，即我自己

                DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);// 事物隔离级别，开启新事务
                TransactionStatus status = txManager.getTransaction(def);// 获得事务状态
                try{
                    //删除friendRoom，触发器自动删除friendItems，自动删除所有的房间message
                    friendDao.deleteFriendRoom(friendRoomId);
                    //删除双方的chatItems
                    chatItemDao.deleteFriendChatItems(userId, friendId);
                    //-----------------------------------向好友双方发送删除好友系统消息------------------------------
                    String userSysNotId = UUID.getUUID();
                    String friendSysNotId = UUID.getUUID();
                    //找到成员对应的系统通知chatItem
                    String userSysChatItemId = chatItemDao.findSystemByUserId(userId).getId();
                    String friendSysChatItemId = chatItemDao.findSystemByUserId(friendId).getId();
                    //生成双方的系统消息
                    String userSysMsg = "你已经将好友 “"+ my_friend.getRemark() +"” 从好友列表中移除。";
                    String friendSysMsg = "你的好友 “"+ him_friend.getRemark() +"” 已经将你从好友列表中移除。";
                    //插入系统消息和接受者
                    systemNotificationDao.insertReceiver(UUID.getUUID(), userSysNotId, userId);
                    systemNotificationDao.insertReceiver(UUID.getUUID(), friendSysNotId, friendId);
                    //插入双方的系统消息
                    systemNotificationDao.insertNotification(
                            new SystemNotification(
                                    userSysNotId,
                                    userSysMsg
                            )
                    );
                    systemNotificationDao.insertNotification(
                            new SystemNotification(
                                    friendSysNotId,
                                    friendSysMsg
                            )
                    );

                    //更新双方的chatItem
                    chatItemDao.updateTimeAndSysNotId(userSysChatItemId, userSysNotId);
                    chatItemDao.updateTimeAndSysNotId(friendSysChatItemId, friendSysNotId);

                    //获取双方的session,下面多次发送将要用到
                    OnlineSession mySession = getSessionService.getUserSession(userId);
                    OnlineSession friendSession = getSessionService.getUserSession(friendId);
                    //加载新的系统消息,删除好友在外部执行，所以双方好友插未读，都用sendToFriend发送
                    onlineMsgService.sendToFriend(
                            mySession,
                            userSysChatItemId,
                            userSysNotId,
                            "load_new_notification"
                    );
                    onlineMsgService.sendToFriend(
                            friendSession,
                            friendSysChatItemId,
                            friendSysNotId,
                            "load_new_notification"
                    );

                    //发送指令给前端，前端自动删除friendChatItem
                    onlineMsgService.sendCommand(//我将好友删除
                            mySession,
                            "friendId",
                            friendId,
                            "remove_friend"
                    );
                    onlineMsgService.sendCommand(//好友将我删除
                            friendSession,
                            "friendId",
                            userId,
                            "remove_friend"
                    );
                    message =  "Remove success";
                    //事务提交
                    txManager.commit(status);
                }catch(Exception e){
                    message =  "Remove fail";
                    //事务回滚
                    txManager.rollback(status);
                }
            }
        }

        return message;
    }
}
