package com.impte.wecard.biz.qq.impl;

import com.impte.wecard.biz.qq.NewQQUserService;
import com.impte.wecard.dao.ChatItemDao;
import com.impte.wecard.dao.GroupDao;
import com.impte.wecard.dao.UserDao;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.UUID;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Service
@AllArgsConstructor
public class NewQQUserServiceImpl implements NewQQUserService {

    private final UserDao userDao;

    private final DataSourceTransactionManager txManager;

    private final ChatItemDao chatItemDao;

    private final GroupDao groupDao;

    @Override
    public User NewQQUser(String userId, String username, String gender, String nickname, String avatarUrl, String city) {
        User dataUser = null;
        if (username != null && gender != null && nickname != null && avatarUrl != null && city != null){
            User user = new User(
                    userId,
                    username,
                    "undefined",
                    "***********",
                    nickname,
                    gender,
                    city,
                    avatarUrl
            );
            //插入qq用户
            userDao.newQQUser(user);
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);// 事物隔离级别，开启新事务
            TransactionStatus status = txManager.getTransaction(def);// 获得事务状态
            try{
                //插入user的chatItem
                //插入系统chatItem
                chatItemDao.insertSystemChatItem(UUID.getUUID(), userId);
                //插入请求通知chatItem
                chatItemDao.insertInformChatItem(UUID.getUUID(), userId);
                //新用户添加默认分组
                groupDao.insertGroup(UUID.getUUID(), "我的好友", userId);
                //获取qq用户信息
                dataUser = userDao.loginGetUser(userId);
                txManager.commit(status);
            }catch(Exception e){
                txManager.rollback(status);
            }
        }
        return dataUser;
    }
}
