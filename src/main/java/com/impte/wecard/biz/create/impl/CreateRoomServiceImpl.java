package com.impte.wecard.biz.create.impl;

import com.impte.wecard.biz.chat.OnlineMsgService;
import com.impte.wecard.biz.create.CreateRoomService;
import com.impte.wecard.dao.ChatItemDao;
import com.impte.wecard.dao.RoomDao;
import com.impte.wecard.dao.RoomMemberDao;
import com.impte.wecard.dao.UserDao;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import com.impte.wecard.utils.SpecialChar;
import com.impte.wecard.utils.UUID;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Service
@AllArgsConstructor
public class CreateRoomServiceImpl implements CreateRoomService {

    private final RoomDao roomDao;
    private final UserDao userDao;
    private final RoomMemberDao roomMemberDao;
    private final ChatItemDao chatItemDao;
    private final OnlineMsgService onlineMsgService;
    private final DataSourceTransactionManager txManager;

    @Override
    public String createRoom(String roomName) {
        String message;

        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (roomName == null || roomName.equals("")){
            message = "RoomName cannot be empty";
        }else if (roomName.length() > 24) {
            message = "RoomName is too long";
        }else if (SpecialChar.usernameContainSpecialChar(roomName)){
            message = "RoomName contains special char";
        }else {
            String userId = user.getId();
            //查找用户等级
            int userGrade = userDao.findUserGrade(userId);
            //查找用户所拥有的房间数量
            int roomNum = roomMemberDao.findOwnerRoomsNum(userId);
            if (userGrade <= 0 || userGrade > 15){
                message = "Grade error";
            }else if (roomNum >= userGrade){
                message = "Cannot create more rooms";
            }else {
                DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);// 事物隔离级别，开启新事务
                TransactionStatus status = txManager.getTransaction(def);// 获得事务状态
                try{
                    //1--插入房间
                    String roomId = UUID.getUUID();
                    roomDao.insertRoom(roomId, roomName, user.getId());
                    //2--插入roomMember
                    roomMemberDao.insertRoomMember(UUID.getUUID(), "owner", user.getUsername(), roomId, userId);
                    //3--插入chatItem
                    String chatItemId = UUID.getUUID();
                    chatItemDao.insertRoomChatItem(chatItemId, roomId, userId);
                    //4--向用户发送添加chatItem指令
                    onlineMsgService.sendCommand(userId, "chatItemId", chatItemId, "load_new_chatItem");
                    //5--向用户发送添加room指令
                    onlineMsgService.sendCommand(userId, "roomId", roomId, "load_new_room");
                    message = "Create success";
                    //事务提交
                    txManager.commit(status);
                }catch(Exception e){
                    message = "Create fail";
                    //事务回滚
                    txManager.rollback(status);
                }
            }
        }
        return message;
    }
}
