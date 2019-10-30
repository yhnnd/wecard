package com.impte.wecard.biz.friend.impl;

import com.impte.wecard.biz.chat.OnlineMsgService;
import com.impte.wecard.biz.friend.RefuseService;
import com.impte.wecard.dao.ChatItemDao;
import com.impte.wecard.dao.RequestDao;
import com.impte.wecard.dao.RoomMemberDao;
import com.impte.wecard.domain.po.ChatItem;
import com.impte.wecard.domain.po.Request;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Service
@AllArgsConstructor
public class RefuseServiceImpl implements RefuseService{

    private final RequestDao requestDao;
    private final OnlineMsgService onlineMsgService;
    private final ChatItemDao chatItemDao;
    private final RoomMemberDao roomMemberDao;
    private final DataSourceTransactionManager txManager;

    @Override
    public String refuseRequest(String requestId) {
        String message;
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (requestId == null || requestId.equals("")){
            message = "RequestId cannot be empty";
        }else {
            //验证当前用户是否有资格处理这条请求
            String verResult = requestDao.verifyHandler(requestId, user.getId());
            String replierId = user.getId();
            if (verResult == null){
                message = "Request permission error";
            }else {
                //找到request
                Request request = requestDao.findById(requestId);
                String requesterId = request.getRequester().getId();

                String type = request.getType();
                if (type.equals("friend")){
                    message = refuseFriendRequest(requesterId, replierId, requestId);
                }else if (type.equals("room")){
                    message = refuseRoomRequest(requesterId, replierId, requestId, request.getRoom().getId());
                }else {
                    message = "Request type error";
                }
            }
        }
        return message;
    }

    private String refuseFriendRequest(String requesterId, String replierId, String requestId) {
        String message;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);// 事物隔离级别，开启新事务
        TransactionStatus status = txManager.getTransaction(def);// 获得事务状态
        try{
            //更新request表，设置status为refuse,设置回复者id
            requestDao.setStatusAndReplierId(requestId, "refuse", replierId);
            //获取我自己的对应的通知聊天栏
            ChatItem requesterInformChatItem = chatItemDao.findInformByUserId(requesterId);
            //获取回复者的对应的通知聊天栏
            ChatItem replierInformChatItem = chatItemDao.findInformByUserId(replierId);
            //更新自己数据库的聊天栏
            chatItemDao.updateTimeAndRequestId(requesterInformChatItem.getId(), requestId);
            //更新好友数据库的聊天栏
            chatItemDao.updateTimeAndRequestId(replierInformChatItem.getId(), requestId);
            //向好友和自己发送拒绝消息
            onlineMsgService.sendToFriend(
                    requesterId,
                    requesterInformChatItem.getId(),
                    requestId,
                    "update_request"
            );
            onlineMsgService.sendToSelf(
                    replierId,
                    replierInformChatItem.getId(),
                    requestId,
                    "update_request"
            );
            //设置请求结果为请求成功
            message = "Refuse success";
            txManager.commit(status);
        }catch(Exception e){
            //设置message为接受失败
            message = "Refuse fail";
            //失败则事务回滚
            txManager.rollback(status);
        }
        return message;
    }

    private String refuseRoomRequest(String requesterId, String replierId, String requestId, String roomId) {
        String message;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);// 事物隔离级别，开启新事务
        TransactionStatus status = txManager.getTransaction(def);// 获得事务状态
        try{
            //更新request表，设置status为refuse,设置回复者id
            requestDao.setStatusAndReplierId(requestId, "refuse", replierId);

            //--------------------------------------第一步要发送给请求者--------------------------------------------------
            //找到请求者对应的chatItem
            ChatItem reqInformChatItem = chatItemDao.findInformByUserId(requesterId);
            String reqInformChatItemId = reqInformChatItem.getId();
            //更新自己数据库的聊天栏
            chatItemDao.updateTimeAndRequestId(reqInformChatItemId, requestId);
            onlineMsgService.sendToFriend(
                    requesterId,
                    reqInformChatItemId,
                    requestId,
                    "update_request"
            );

            //--------------------------------------第二步发送给所有管理员------------------------------------------------
            //找到所有的管理员
            List<String> adminIds = roomMemberDao.findAdminIdsByRoomId(roomId);
            if (adminIds != null && adminIds.size() != 0){
                for (String adminId: adminIds){
                    //找到所有管理员对应的chatItem
                    ChatItem adminInformChatItem = chatItemDao.findInformByUserId(adminId);
                    String adminInformChatItemId = adminInformChatItem.getId();
                    //更新自己数据库的聊天栏
                    chatItemDao.updateTimeAndRequestId(adminInformChatItemId, requestId);

                    if (adminId.equals(replierId)){
                        onlineMsgService.sendToSelf(//发送给自己，不用插未读
                                replierId,
                                adminInformChatItemId,
                                requestId,
                                "update_request"
                        );
                    }else {
                        onlineMsgService.sendToFriend(//发送给别人，需要插未读
                                adminId,
                                adminInformChatItemId,
                                requestId,
                                "update_request"
                        );
                    }
                }
            }

            //设置请求结果为请求成功
            message = "Refuse success";
            txManager.commit(status);
        }catch(Exception e){
            //设置message为接受失败
            message = "Refuse fail";
            //失败则事务回滚
            txManager.rollback(status);
        }

        return message;
    }
}
