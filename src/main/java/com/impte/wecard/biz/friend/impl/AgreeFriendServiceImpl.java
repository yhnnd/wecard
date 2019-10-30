package com.impte.wecard.biz.friend.impl;

import com.impte.wecard.biz.chat.GetSessionService;
import com.impte.wecard.biz.chat.OnlineMsgService;
import com.impte.wecard.biz.friend.AgreeFriendService;
import com.impte.wecard.dao.ChatItemDao;
import com.impte.wecard.dao.FriendDao;
import com.impte.wecard.dao.GroupDao;
import com.impte.wecard.dao.RequestDao;
import com.impte.wecard.domain.po.ChatItem;
import com.impte.wecard.domain.po.Group;
import com.impte.wecard.domain.po.Request;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import com.impte.wecard.utils.SpecialChar;
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
public class AgreeFriendServiceImpl implements AgreeFriendService {

    private final RequestDao requestDao;

    private final FriendDao friendDao;

    private final GroupDao groupDao;

    private final ChatItemDao chatItemDao;

    private final OnlineMsgService onlineMsgService;

    private final GetSessionService getSessionService;

    private final DataSourceTransactionManager txManager;

    @Override
    public String agreeFriendRequest(String groupId, String remark, String requestId) {
        String message;
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        //对输入的groupId，remark，requestId进行格式判断，排除非法输入
        if (requestId == null || requestId.equals("")){
            message = "RequestId cannot be empty";
        }else if (groupId == null || groupId.equals("")){
            message = "GroupId cannot be empty";
        }else if (remark != null && SpecialChar.nicknameContainSpecialChar(remark)){
            message = "Remark contain special char";
        }else if (remark != null && remark.length() > 12){
            message = "Remark is too long";
        }else {
            //取出userId
            String receiverId = user.getId();
            //验证request是否存在
            Request request = requestDao.findById(requestId);
            //验证分组是否存在
            Group group = groupDao.findById(groupId);
            //如果请求为空或者请求类型不匹配，则返回不存在
            if (request == null || request.getType() == null || !request.getType().equals("friend")){
                message = "Request does not exist";
            }else if (request.getReceiver() == null || !request.getReceiver().getId().equals(receiverId)){
                message = "Request permission error";
            }else if (request.getRequestStatus().equals("accept")){
                message = "Request has been agreed";
            }else if (request.getRequestStatus().equals("refuse")){
                message = "Request has been refused";
            }else if (group == null){
                message = "Group does not exist";
            }else if (!group.getUser().getId().equals(receiverId)){
                message = "Group permission error";
            }else {
                //取出requester,方便以后使用
                User requester = request.getRequester();
                String requesterId = requester.getId();
                //再次确认接受者和发送者是否已经是好友
                String friendRoomId = friendDao.findFriendsRoomId(requester.getId(), receiverId);
                if (friendRoomId != null){
                    message = "You have already been friends";
                }else {
                    //如果朋友备注为空，备注默认为用户名
                    if (remark == null || remark.equals("")){
                        remark = requester.getUsername();
                    }

                    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);// 事物隔离级别，开启新事务
                    TransactionStatus status = txManager.getTransaction(def);// 获得事务状态
                    try{
                        OnlineSession requesterSession = getSessionService.getUserSession(requesterId);
                        OnlineSession receiverSession = getSessionService.getUserSession(receiverId);
                        //----------------------------------------插入好友------------------------------------------
                        String friendOneId = UUID.getUUID();
                        String friendTwoId = UUID.getUUID();
                        //插入请求者的好友
                        //接受者是请求者的好友，备注是请求者给的备注，分组是请求者给的分组，user就是请求者的好友，也就是接受者
                        friendDao.insertFriendItem(
                                friendOneId,
                                request.getRequestRemark(),
                                request.getRequestGroup().getId(),
                                receiverId,
                                requesterId
                        );
                        //插入接受者的好友
                        friendDao.insertFriendItem(
                                friendTwoId,
                                remark,
                                groupId,
                                requesterId,
                                receiverId
                        );
                        //插入friendRoom
                        friendDao.insertFriendRoom(UUID.getUUID(), friendOneId, friendTwoId);

                        //----------------------------------插入接受者的chatItem------------------------------------
                        String receiverChatItemId = UUID.getUUID();
                        chatItemDao.insertFriendChatItem(
                                receiverChatItemId,
                                requesterId,
                                receiverId
                        );
                        //插入请求者的chatItem
                        String requesterChatItemId = UUID.getUUID();
                        chatItemDao.insertFriendChatItem(
                                requesterChatItemId,
                                receiverId,
                                requesterId
                        );
                        //---------------------------------------发送reply给两个用户-----------------------------------
                        //更新请求表，把请求状态设置为agree，并设置replierId
                        requestDao.setStatusAndReplierId(requestId, "agree", receiverId);
                        //获取请求者对应的通知聊天栏
                        ChatItem requesterInformChatItem = chatItemDao.findInformByUserId(requesterId);
                        //获取接收者的对应的通知聊天栏
                        ChatItem receiverInformChatItem = chatItemDao.findInformByUserId(receiverId);
                        //更新请求者数据库的聊天栏
                        chatItemDao.updateTimeAndRequestId(requesterInformChatItem.getId(), requestId);
                        //更新自己数据库的聊天栏
                        chatItemDao.updateTimeAndRequestId(receiverInformChatItem.getId(), requestId);
                        //给朋友发送接受请求消息
                        onlineMsgService.sendToFriend(
                                requesterSession,
                                requesterInformChatItem.getId(),
                                requestId,
                                "update_request"
                        );
                        //给自己发送请求消息
                        onlineMsgService.sendToSelf(
                                receiverSession,
                                receiverInformChatItem.getId(),
                                requestId,
                                "update_request"
                        );
                        //-------------------------------向两位用户发送添加chatItem指令-------------------------------
                        onlineMsgService.sendCommand(
                                receiverSession,
                                "chatItemId",
                                receiverChatItemId,
                                "load_new_chatItem"
                        );
                        onlineMsgService.sendCommand(
                                requesterSession,
                                "chatItemId",
                                requesterChatItemId,
                                "load_new_chatItem"
                        );
                        //-------------------------------向两位用户发送更新group指令-------------------------------
                        onlineMsgService.sendCommand(
                                receiverSession,
                                "groupId",
                                groupId,
                                "update_group"
                        );
                        String requestGroupId = request.getRequestGroup().getId();
                        onlineMsgService.sendCommand(
                                requesterSession,
                                "groupId",
                                requestGroupId,
                                "update_group"
                        );

                        //设置message为同意加好友成功
                        message = "Agree success";
                        //提交事务
                        txManager.commit(status);
                    }catch(Exception e){
                        //设置message为同意加好友失败
                        message = "Agree fail";
                        //失败者事务回滚
                        txManager.rollback(status);
                    }
                }
            }
        }
        return message;
    }
}
