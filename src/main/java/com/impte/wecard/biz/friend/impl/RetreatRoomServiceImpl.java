package com.impte.wecard.biz.friend.impl;

import com.impte.wecard.biz.chat.GetSessionService;
import com.impte.wecard.biz.chat.OnlineMsgService;
import com.impte.wecard.biz.friend.RetreatRoomService;
import com.impte.wecard.dao.ChatItemDao;
import com.impte.wecard.dao.RoomDao;
import com.impte.wecard.dao.RoomMemberDao;
import com.impte.wecard.dao.SystemNotificationDao;
import com.impte.wecard.domain.po.ChatItem;
import com.impte.wecard.domain.po.Room;
import com.impte.wecard.domain.po.RoomMember;
import com.impte.wecard.domain.po.SystemNotification;
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
public class RetreatRoomServiceImpl implements RetreatRoomService {

    private final RoomMemberDao roomMemberDao;
    private final SystemNotificationDao systemNotificationDao;
    private final RoomDao roomDao;
    private final ChatItemDao chatItemDao;
    private final OnlineMsgService onlineMsgService;
    private final GetSessionService getSessionService;
    private final DataSourceTransactionManager txManager;

    @Override
    public String retreatRoom(String roomId) {
        String message;
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (roomId == null || roomId.equals("")){
            message = "RoomId cannot be empty";
        }else {
            String userId = user.getId();
            //验证群是否存在
            Room room = roomDao.verifyRoomExist(roomId);
            //验证当前用户是不是房间成员
            RoomMember roomMember = roomMemberDao.verifyRoomMember(userId, roomId);
            if (room == null){
                message = "Room does not exist";
            }else if (roomMember == null){
                message = "You are not member";
            }else if (roomMember.getRole().equals("owner")){
                message = "Owner cannot quit room";
            }else {
                DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);// 事物隔离级别，开启新事务
                TransactionStatus status = txManager.getTransaction(def);// 获得事务状态
                try{
                    //删除群成员, 数据库触发器自动删除chatItem
                    roomMemberDao.deleteByRoomIdAndUserId(userId, roomId);

                    //------------------------3--插入系统通知表（功能例：你已被XXX移出群XXX）-------------------------------------
                    //先插入给退出者的系统消息
                    String userSysMsg = "你已经退出群 “" + room.getRoomName() + "” 。";
                    String userSysNotId = UUID.getUUID();
                    systemNotificationDao.insertReceiver(UUID.getUUID(), userSysNotId, userId);
                    systemNotificationDao.insertNotification(
                            new SystemNotification(
                                    userSysNotId,
                                    userSysMsg
                            )
                    );

                    //再插入给管理员的系统消息
                    String adminSysMsg ="“" + roomMember.getRoomRemark() + "” 已经退出群 “" + room.getRoomName() + "”。";
                    String adminSysNotId = UUID.getUUID();
                    //插入系统消息，但是不插入receivers,在循环里插入接收人
                    systemNotificationDao.insertNotification(
                            new SystemNotification(
                                    adminSysNotId,
                                    adminSysMsg
                            )
                    );

                    List<String> adminIds = roomMemberDao.findAdminIdsByRoomId(room.getId());
                    if (adminIds != null && adminIds.size() != 0){
                        for (String adminId: adminIds){
                            //插入notification receiver表
                            String receiverTableId = UUID.getUUID();
                            systemNotificationDao.insertReceiver(receiverTableId, adminSysNotId, adminId);
                            //获取所有管理员对应的通知聊天栏
                            ChatItem adminSysChatItem = chatItemDao.findSystemByUserId(adminId);
                            if (adminSysChatItem != null){
                                String adminSysChatItemId = adminSysChatItem.getId();
                                //更新管理员的数据库的聊天栏
                                chatItemDao.updateTimeAndSysNotId(adminSysChatItemId, adminSysNotId);
                                //发送加载新通知的命令给管理员
                                onlineMsgService.sendToFriend(
                                        adminId,
                                        adminSysChatItemId,
                                        adminSysNotId,
                                        "load_new_notification"
                                );
                            }
                        }
                    }

                    //----------------------------------------发送消息给退出者-------------------------------------------
                    //获取退出者的session
                    OnlineSession userSession = getSessionService.getUserSession(userId);
                    //获取退出者对应的通知聊天栏
                    ChatItem userSysChatItem = chatItemDao.findSystemByUserId(userId);
                    String userSysChatItemId = userSysChatItem.getId();
                    //更新被删除者数据库的聊天栏
                    chatItemDao.updateTimeAndSysNotId(userSysChatItemId, userSysNotId);
                    //发送加载新通知的命令给退出者
                    onlineMsgService.sendToFriend(
                            userSession,
                            userSysChatItem.getId(),
                            userSysNotId,
                            "load_new_notification"
                    );

                    //-------------------------给被删除者发送删除room指令，前端自动删除chatItem------------------------
                    onlineMsgService.sendCommand(//移除room
                            userSession,
                            "roomId",
                            roomId,
                            "remove_room"
                    );

                    message = "Quit success";
                    //事务提交
                    txManager.commit(status);
                }catch(Exception e){
                    CurrentUtil.getSession().removeAttribute("user");
                    message =  "Quit fail";
                    //事务回滚
                    txManager.rollback(status);
                }
            }
        }

        return message;
    }
}
