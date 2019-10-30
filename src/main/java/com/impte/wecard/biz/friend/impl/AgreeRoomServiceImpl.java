package com.impte.wecard.biz.friend.impl;

import com.impte.wecard.biz.chat.GetSessionService;
import com.impte.wecard.biz.chat.OnlineMsgService;
import com.impte.wecard.biz.friend.AgreeRoomService;
import com.impte.wecard.dao.ChatItemDao;
import com.impte.wecard.dao.RequestDao;
import com.impte.wecard.dao.RoomMemberDao;
import com.impte.wecard.domain.po.ChatItem;
import com.impte.wecard.domain.po.Request;
import com.impte.wecard.domain.po.Room;
import com.impte.wecard.domain.po.RoomMember;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import com.impte.wecard.utils.UUID;
import com.impte.wecard.utils.websocket.OnlineSession;
import java.util.List;
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
public class AgreeRoomServiceImpl implements AgreeRoomService {

    private final RequestDao requestDao;

    private final RoomMemberDao roomMemberDao;

    private final DataSourceTransactionManager txManager;

    private final ChatItemDao chatItemDao;

    private final GetSessionService getSessionService;

    private final OnlineMsgService onlineMsgService;

    @Override
    public String agreeRoomRequest(String requestId) {
        String message;
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        //对输入的requestId进行格式判断，排除非法输入
        if (requestId == null || requestId.equals("")){
            message = "RequestId cannot be empty";
        }else {
            //取出回复者id
            String replierId = user.getId();
            //验证request是否存在
            Request request = requestDao.findById(requestId);
            //验证回复者是否有资格处理这条请求
            String qualification = requestDao.verifyHandler(requestId, replierId);
            //如果请求为空或者请求类型不匹配，则返回不存在
            if (request == null || request.getType() == null || !request.getType().equals("room")){
                message = "Request does not exist";
            }else if (qualification == null){
                message = "You are not admin";
            }else if (request.getRequestStatus().equals("accept")){
                message = "Request has been agreed";
            }else if (request.getRequestStatus().equals("refuse")){
                message = "Request has been refused";
            }else {
                //取出requester,方便以后使用
                User requester = request.getRequester();
                String requesterId = requester.getId();
                //取出请求的房间
                Room room = request.getRoom();
                //验证发送请求的人是否已经是群成员
                RoomMember requestRoomMember = roomMemberDao.verifyRoomMember(requester.getId(), room.getId());
                //验证当前管理者是否是admin
                RoomMember adminRoomMember = roomMemberDao.verifyRoomAdmin(replierId, room.getId());
                if (requestRoomMember != null){
                    message = "Requester have been a member of this room.";
                }else if (adminRoomMember == null){
                    message = "You are not admin";
                }else {
                    //群名称默认设置为用户名
                    String roomRemark = requester.getUsername();

                    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);// 事物隔离级别，开启新事务
                    TransactionStatus status = txManager.getTransaction(def);// 获得事务状态
                    try{
                        //1--找到请求发送者的session
                        OnlineSession requesterSession = getSessionService.getUserSession(requesterId);
                        //----------------------------------------------插入------------------------------------------------------
                        //2--插入群成员
                        roomMemberDao.insertRoomMember(
                                UUID.getUUID(),
                                "member",//默认为群成员，如果是新建群，建立者就是群主
                                roomRemark,
                                room.getId(),
                                requesterId
                        );
                        //3--插入请求者的chatItem
                        String requesterChatItemId = UUID.getUUID();
                        chatItemDao.insertRoomChatItem(
                                requesterChatItemId,
                                room.getId(),
                                requesterId
                        );
                        //更新请求表，把请求状态设置为agree
                        requestDao.setStatusAndReplierId(requestId, "agree", replierId);
                        //---------------------------------------向请求者发送回复----------------------------------------
                        //获取请求者对应的通知聊天栏
                        ChatItem requesterInformChatItem = chatItemDao.findInformByUserId(requesterId);
                        //更新请求者数据库的聊天栏
                        chatItemDao.updateTimeAndRequestId(requesterInformChatItem.getId(), requestId);
                        //---------------------------------------向管理员发送回复-----------------------------------------
                        //找到所有的管理员Id
                        List<String> adminIds = roomMemberDao.findAdminIdsByRoomId(room.getId());
                        if (adminIds != null && adminIds.size() > 0) {
                            for (String adminId : adminIds) {
                                //获取管理员的对应的通知聊天栏
                                ChatItem adminInformChatItem = chatItemDao.findInformByUserId(adminId);
                                //不为空则发送消息
                                if (adminInformChatItem != null){
                                    //更新管理员数据库的聊天栏
                                    chatItemDao.updateTimeAndRequestId(adminInformChatItem.getId(), requestId);
                                    if (adminId.equals(replierId)){
                                        //如果是管理员是自己，
                                        onlineMsgService.sendToSelf(
                                                adminId,
                                                adminInformChatItem.getId(),
                                                requestId,
                                                "update_request"
                                        );
                                    }else {
                                        //给其他管理员发送接受请求消息
                                        onlineMsgService.sendToFriend(
                                                adminId,
                                                adminInformChatItem.getId(),
                                                requestId,
                                                "update_request"
                                        );
                                    }
                                }
                            }
                        }
                        //给发送者发送接受请求消息
                        onlineMsgService.sendToFriend(
                                requesterSession,
                                requesterInformChatItem.getId(),
                                requestId,
                                "update_request"
                        );

                        //4-----------------------------------向请求者发送添加新群chatItem指令----------------------------------
                        onlineMsgService.sendCommand(
                                requesterSession,
                                "chatItemId",
                                requesterChatItemId,
                                "load_new_chatItem"
                        );
                        //5--------------------------------------------向请求者发送添加群指令--------------------------------
                        onlineMsgService.sendCommand(
                                requesterSession,
                                "roomId",
                                room.getId(),
                                "load_new_room"
                        );

                        //设置message为success接受入群成功,提交事务
                        message = "Agree success";
                        txManager.commit(status);
                    }catch(Exception e){
                        //设置message为fail接受入群失败,事务回滚
                        message = "Agree fail";
                        txManager.rollback(status);
                    }

                }
            }
        }
        return message;
    }
}
