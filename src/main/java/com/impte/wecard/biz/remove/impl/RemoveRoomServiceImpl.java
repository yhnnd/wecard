package com.impte.wecard.biz.remove.impl;

import com.impte.wecard.biz.chat.OnlineMsgService;
import com.impte.wecard.biz.remove.RemoveRoomService;
import com.impte.wecard.dao.ChatItemDao;
import com.impte.wecard.dao.RoomDao;
import com.impte.wecard.dao.RoomMemberDao;
import com.impte.wecard.dao.SystemNotificationDao;
import com.impte.wecard.domain.po.Room;
import com.impte.wecard.domain.po.RoomMember;
import com.impte.wecard.domain.po.SystemNotification;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import com.impte.wecard.utils.UUID;
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
public class RemoveRoomServiceImpl implements RemoveRoomService {

    private final RoomDao roomDao;

    private final RoomMemberDao roomMemberDao;

    private final DataSourceTransactionManager txManager;

    private final SystemNotificationDao systemNotificationDao;

    private final ChatItemDao chatItemDao;

    private final OnlineMsgService onlineMsgService;

    @Override
    public String removeRoom(String roomId) {
        String message;
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (roomId == null || roomId.equals("")){
            message = "RoomId cannot be empty";
        }else {
            String userId = user.getId();
            //查找群
            Room room = roomDao.findById(roomId);
            RoomMember owner = roomMemberDao.verifyRoomOwner(userId, roomId);
            if (room == null){
                message = "Room does not exist";
            }else if (owner == null){
                message = "You are not owner";
            }else {
                //先找到所有的房间成员
                List<RoomMember> roomMembers = roomMemberDao.findRoomMembers(roomId);

                DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);// 事物隔离级别，开启新事务
                TransactionStatus status = txManager.getTransaction(def);// 获得事务状态
                try{
                    //设置房间状态为不存在
                    roomDao.updateStatusNotExist(roomId);
                    //删除所有的房间成员,触发器自动删除chatItem
                    roomMemberDao.deleteAllMembersByRoomId(roomId);

                    //生成发给每个用户的系统信息
                    String memberSysMsg = "群主 “" + owner.getRoomRemark() + "” 已将群 “" + room.getRoomName() + "” 解散。";
                    //向所有房间成员发送删除房间请求,删除chatItem前端自动删除
                    if (roomMembers != null && roomMembers.size() != 0){
                        for (RoomMember roomMember: roomMembers){
                            String roomMemberId = roomMember.getId();

                            String memberSysNotId = UUID.getUUID();
                            //找到成员对应的系统通知chatItem
                            String SysChatItemId = chatItemDao.findSystemByUserId(roomMemberId).getId();
                            //插入系统消息和接受者
                            systemNotificationDao.insertReceiver(UUID.getUUID(), memberSysNotId, roomMemberId);
                            systemNotificationDao.insertNotification(
                                    new SystemNotification(
                                            memberSysNotId,
                                            memberSysMsg
                                    )
                            );
                            //向每个成员发送系统消息
                            onlineMsgService.sendToFriend(
                                    roomMemberId,
                                    SysChatItemId,
                                    memberSysNotId,
                                    "load_new_notification"
                            );
                        }
                    }
                    message =  "Remove success";
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

        return message;
    }
}
