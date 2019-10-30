package com.impte.wecard.biz.begin.impl;

import com.impte.wecard.biz.begin.RefreshService;
import com.impte.wecard.dao.ChatItemDao;
import com.impte.wecard.dao.GroupDao;
import com.impte.wecard.dao.RoomDao;
import com.impte.wecard.dao.UnreadDao;
import com.impte.wecard.dao.UserDao;
import com.impte.wecard.domain.po.ChatItem;
import com.impte.wecard.domain.po.Group;
import com.impte.wecard.domain.po.Room;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class RefreshServiceImpl implements RefreshService {

    private final UserDao userDao;

    private final GroupDao groupDao;

    private final UnreadDao unreadDao;

    private final ChatItemDao chatItemDao;

    private final RoomDao roomDao;

    private final DataSourceTransactionManager txManager;

    @Override
    public Map<String, Object> refresh() {
        String message;
        Map<String, Object> map = new HashMap<>();
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);// 事物隔离级别，开启新事务
        TransactionStatus status = txManager.getTransaction(def);// 获得事务状态
        try{
            String userId = user.getId();
            //2--找到User基本信息
            user = userDao.loginGetUser(userId);
            //4--找到分组信息
            List<Group> groups = groupDao.findByUserId(userId);
            //5--找到未读数量
            int allUnreadNum = unreadDao.findUnreadCount(userId);
            //6--找到聊天列表
            List<ChatItem> chatList = chatItemDao.findByUserId(userId);
            //7--找到所有群
            List<Room> rooms = roomDao.findUserRooms(userId);
            //8--找到所有chatItems,加入user
            List<ChatItem> chatItems = chatItemDao.findAllUserChatItems(userId);

            map.put("user", user);
            map.put("rooms", rooms);
            map.put("groups", groups);
            map.put("chatList", chatList);
            map.put("allUnreadNum", allUnreadNum);
            map.put("allChatItems", chatItems);
            message = "Refresh success";
            //事务提交
            txManager.commit(status);
        }catch(Exception e){
            message = "Refresh fail";
            //事务回滚
            txManager.rollback(status);
        }

        map.put("message", message);

        return map;
    }
}
