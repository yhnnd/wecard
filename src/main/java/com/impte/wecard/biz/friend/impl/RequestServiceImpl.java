package com.impte.wecard.biz.friend.impl;

import com.impte.wecard.biz.chat.OnlineMsgService;
import com.impte.wecard.biz.friend.RequestService;
import com.impte.wecard.dao.ChatItemDao;
import com.impte.wecard.dao.FriendDao;
import com.impte.wecard.dao.GroupDao;
import com.impte.wecard.dao.RequestDao;
import com.impte.wecard.dao.RoomDao;
import com.impte.wecard.dao.RoomMemberDao;
import com.impte.wecard.dao.UserDao;
import com.impte.wecard.domain.po.ChatItem;
import com.impte.wecard.domain.po.Group;
import com.impte.wecard.domain.po.Room;
import com.impte.wecard.domain.po.RoomMember;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import com.impte.wecard.utils.SpecialChar;
import com.impte.wecard.utils.UUID;
import java.util.List;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Service
@AllArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final UserDao userDao;
    private final RequestDao requestDao;
    private final OnlineMsgService onlineMsgService;
    private final FriendDao friendDao;
    private final RoomMemberDao roomMemberDao;
    private final RoomDao roomDao;
    private final GroupDao groupDao;
    private final DataSourceTransactionManager txManager;
    private final ChatItemDao chatItemDao;

    @Override
    public String makeFriendRequest(String verifyName, String groupId, String remark, String requestMsg) {
        String message;
        User requester = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (verifyName == null || "".equals(verifyName)){
            message = "Username cannot be empty";
        }else if (groupId == null || "".equals(groupId)){
            message = "GroupId cannot be empty";
        }else if (remark != null && SpecialChar.nicknameContainSpecialChar(remark)){
            message = "Remark contains special char";
        }else if (remark != null && remark.length() > 12){
            message = "Remark is too long";
        }else {
            String requesterId = requester.getId();
            //验证用户是否存在
            User receiver_1 = userDao.verifyUserExistByUsername(verifyName);
            User receiver_2 = userDao.verifyUserExistByPhone(verifyName);
            User receiver = null;
            if (receiver_1 != null){
                receiver = receiver_1;
            }else if (receiver_2 != null){
                receiver = receiver_2;
            }
            //验证分组是否存在
            Group group = groupDao.findById(groupId);
            if (receiver == null){
                message = "Receiver does not exist";
            }else if (receiver.getId().equals(requester.getId())){
                message = "Receiver is yourself";
            }else if (group == null){
                message = "Group does not exist";
            }else if (!group.getUser().getId().equals(requesterId)){
                message = "Group permission error";
            }else {
                String receiverId = receiver.getId();
                //验证接受者是否已经是发送者的好友
                String friendRoomId = friendDao.findFriendsRoomId(requesterId, receiverId);
                if (friendRoomId != null) {
                    message = "You have already been friends";
                }else{
                    //如果朋友备注为空，备注默认为用户名
                    if (remark == null || remark.equals("")){
                        remark = receiver.getUsername();
                    }

                    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);// 事物隔离级别，开启新事务
                    TransactionStatus status = txManager.getTransaction(def);// 获得事务状态
                    try{
                        //插入请求表
                        String requestId = UUID.getUUID();
                        requestDao.insertFriendRequest(
                                requestId,
                                groupId,
                                remark,
                                requesterId,
                                receiverId,
                                requestMsg
                        );
                        //插入接受者到关联表
                        requestDao.insertReceiver(
                                UUID.getUUID(),
                                receiverId,
                                requestId
                        );

                        //获取我自己的对应的通知聊天栏
                        ChatItem requesterInformChatItem = chatItemDao.findInformByUserId(requesterId);
                        String requesterInformChatItemId = requesterInformChatItem.getId();
                        //获取接收者的对应的通知聊天栏
                        ChatItem receiverInformChatItem = chatItemDao.findInformByUserId(receiverId);
                        String receiverInformChatItemId = receiverInformChatItem.getId();
                        //更新自己数据库的聊天栏
                        chatItemDao.updateTimeAndRequestId(requesterInformChatItemId, requestId);
                        //更新好友数据库的聊天栏
                        chatItemDao.updateTimeAndRequestId(receiverInformChatItemId, requestId);
                        //给朋友发送请求消息
                        onlineMsgService.sendToFriend(
                                receiverId,
                                receiverInformChatItemId,
                                requestId,
                                "load_new_request"
                        );
                        //给自己发送请求消息
                        onlineMsgService.sendToFriend(
                                requesterId,
                                requesterInformChatItemId,
                                requestId,
                                "load_new_request"
                        );
                        //设置result为发送成功
                        message = "Send success";
                        //事务提交
                        txManager.commit(status);
                    }catch(Exception e) {
                        e.printStackTrace();
                        message = "Send Fail";
                        //事务回滚
                        txManager.rollback(status);
                    }
                }
            }
        }
        return message;
    }

    @Override
    public String joinRoomRequest(String roomId, String requestMsg) {
        String message;
        User requester = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (roomId == null || roomId.equals("")){
            message = "RoomId cannot be empty";
        }else {
            String requesterId = requester.getId();
            //验证房间是否存在
            Room room = roomDao.verifyRoomExist(roomId);
            if (room == null){
                message = "Room does not exist";
            }else {
                //验证发送者是否已经在群里
                RoomMember roomMember = roomMemberDao.verifyRoomMember(requesterId, roomId);
                if (roomMember != null) {
                    message = "You have been room member ";
                }else{
                    List<String> adminIds = roomMemberDao.findAdminIdsByRoomId(roomId);
                    if (adminIds.size() == 0) {
                        message = "Request fail";
                    } else {

                        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);// 事物隔离级别，开启新事务
                        TransactionStatus status = txManager.getTransaction(def);// 获得事务状态
                        try{

                            //插入请求表
                            String requestId = UUID.getUUID();
                            requestDao.insertRoomRequest(
                                    requestId,
                                    requesterId,
                                    roomId,
                                    requestMsg
                            );

                            //遍历所有管理员，想所有管理员发送信息
                            for (String adminId: adminIds){
                                //插入接受者关联表
                                requestDao.insertReceiver(
                                        UUID.getUUID(),
                                        adminId,
                                        requestId
                                );
                                //获取管理员的对应的通知聊天栏
                                ChatItem adminInformChatItem = chatItemDao.findInformByUserId(adminId);
                                if (adminInformChatItem != null){
                                    //更新管理员数据库的聊天栏
                                    chatItemDao.updateTimeAndRequestId(adminInformChatItem.getId(), requestId);
                                    //发送请求给所有管理员
                                    onlineMsgService.sendToFriend(
                                            adminId,
                                            adminInformChatItem.getId(),
                                            requestId,
                                            "load_new_request"
                                    );
                                }
                            }

                            //获取我自己的对应的通知聊天栏
                            ChatItem requesterInformChatItem = chatItemDao.findInformByUserId(requesterId);
                            //更新自己数据库的聊天栏
                            String requesterInformChatItemId = requesterInformChatItem.getId();
                            chatItemDao.updateTimeAndRequestId(requesterInformChatItemId, requestId);
                            //自己也收到请求,可能在chatItem之外
                            onlineMsgService.sendToFriend(
                                    requesterId,
                                    requesterInformChatItemId,
                                    requestId,
                                    "load_new_request"
                            );

                            //设置请求结果为请求成功
                            message = "Send success";
                            txManager.commit(status);
                        }catch(Exception e){
                            //设置message为接受失败
                            message = "Send fail";
                            //失败则事务回滚
                            txManager.rollback(status);
                        }
                    }
                }
            }
        }

        return message;
    }
}
