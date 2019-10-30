package com.impte.wecard.dao;

import com.impte.wecard.domain.po.SystemNotification;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemNotificationDao extends GenericDao<SystemNotification, String> {
    List<SystemNotification> findByReceiver(
        @Param("receiverId") String receiverId,
        @Param("offset") int offset,
        @Param("limit") int limit
    );

    //插入系统通知
    int insertNotification(SystemNotification systemNotification);
    //插入系统通知的接收者
    int insertReceiver(
        @Param("id") String id,
        @Param("systemNotificationId") String systemNotificationId,
        @Param("receiverId") String receiverId
    );
}
