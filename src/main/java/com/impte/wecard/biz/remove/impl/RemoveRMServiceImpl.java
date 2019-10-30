package com.impte.wecard.biz.remove.impl;

import com.impte.wecard.biz.chat.GetSessionService;
import com.impte.wecard.biz.chat.OnlineMsgService;
import com.impte.wecard.biz.remove.RemoveRMService;
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

import lombok.AllArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Service
@AllArgsConstructor
public class RemoveRMServiceImpl implements RemoveRMService {

    private final RoomDao roomDao;
    private final RoomMemberDao roomMemberDao;
    private final ChatItemDao chatItemDao;
    private final SystemNotificationDao systemNotificationDao;
    private final OnlineMsgService onlineMsgService;
    private final GetSessionService getSessionService;
    private final DataSourceTransactionManager txManager;

    @Override
    public String removeMemberService(String roomId, String memberId) {
        String message;
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (roomId == null || "".equals(roomId)){
            message = "RoomId cannot be empty";
        }else if (memberId == null || roomId.equals("")){
            message = "MemberId cannot be empty";
        }else {
            String userId = user.getId();
            //验证群是否存在
            Room room = roomDao.verifyRoomExist(roomId);
            if (room == null){
                message = "Room does not exist";
            }else {
                //验证要删除的人是否是群成员
                RoomMember member = roomMemberDao.verifyRoomMember(memberId, roomId);
                //验证操作者是否是管理员
                RoomMember admin = roomMemberDao.verifyRoomAdmin(userId, roomId);
                //验证被删除者是否是管理员
                RoomMember memberAdmin = roomMemberDao.verifyRoomAdmin(memberId, roomId);
                if (member == null){
                    message = "Unknown room member";
                }else if (admin == null){
                    message = "You are not admin";
                }else if (memberAdmin != null && memberAdmin.getRole().equals("owner")){
                    message = "You cannot remove owner";
                }else if (memberAdmin != null && memberAdmin.getRole().equals("admin") && admin.getRole().equals("admin")){
                    message = "Insufficient permissions";
                }else {
//                    //1--找到被删除者对应的chatItem，放在事务外面，在删除之前获得他的id
//                    ChatItem memberToRoomChatItem = chatItemDao.findByObjectId(memberId, roomId);

                    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);// 事物隔离级别，开启新事务
                    TransactionStatus status = txManager.getTransaction(def);// 获得事务状态
                    try{
                        //2--删除群成员, 数据库触发器自动删除chatItem
                        roomMemberDao.deleteByRoomIdAndUserId(memberId, roomId);

                        //------------------------3--插入系统通知表（功能例：你已被XXX移出群XXX）-------------------------------------
                        //先插入给被删除者的系统消息
                        String memberSysMsg = "你已经被管理员 “" + admin.getRoomRemark() + "” 请出群 “" + room.getRoomName() + "”。";
                        String memberSysNotId = UUID.getUUID();
                        systemNotificationDao.insertReceiver(UUID.getUUID(), memberSysNotId, memberId);
                        systemNotificationDao.insertNotification(
                                new SystemNotification(
                                        memberSysNotId,
                                        memberSysMsg
                                )
                        );

                        //-------------再插入处理人的系统信息
                        String adminSysMsg = "管理员 “"+ admin.getRoomRemark() +"” 已经将群成员 “"+ member.getRoomRemark() + "” 请出群 “" + room.getRoomName() + "”。";
                        String adminSysNotId = UUID.getUUID();
                        //插入系统消息，但是不插入receivers,在循环里插入
                        systemNotificationDao.insertNotification(
                                new SystemNotification(
                                        adminSysNotId,
                                        adminSysMsg
                                )
                        );

                        //获取被删除者的session
                        OnlineSession memberSession = getSessionService.getUserSession(memberId);
                        //获取被删除者对应的通知聊天栏
                        ChatItem memberSysChatItem = chatItemDao.findSystemByUserId(memberId);
                        String memberSysChatItemId = memberSysChatItem.getId();
                        //更新被删除者数据库的聊天栏
                        int result = chatItemDao.updateTimeAndSysNotId(memberSysChatItemId, memberSysNotId);
                        //发送加载新通知的命令给被删除者
                        onlineMsgService.sendToFriend(
                                memberSession,
                                memberSysChatItemId,
                                memberSysNotId,
                                "load_new_notification"
                        );

                        List<String> adminIds = roomMemberDao.findAdminIdsByRoomId(room.getId());
                        if (adminIds != null && adminIds.size() != 0){
                            for (String adminId: adminIds){
                                //插入notification receiver表
                                systemNotificationDao.insertReceiver(UUID.getUUID(), adminSysNotId, adminId);
                                //获取所有管理员对应的通知聊天栏
                                ChatItem adminSysChatItem = chatItemDao.findSystemByUserId(adminId);
                                if (adminSysChatItem != null){
                                    String adminSysChatItemId = adminSysChatItem.getId();
                                    //更新数据库的聊天栏
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

                        //-------------------------给被删除者发送删除room指令，前端自动删除chatItem------------------------
                        onlineMsgService.sendCommand(//移除room
                                memberSession,
                                "roomId",
                                roomId,
                                "remove_room"
                        );

                        message = "Remove success";
                        //事务提交
                        txManager.commit(status);
                    }catch(Exception e){
                        CurrentUtil.getSession().removeAttribute("user");
                        message =  "Remove fail";
                        //事务回滚
                        txManager.rollback(status);
                    }
                }
            }
        }

        return message;
    }
}
