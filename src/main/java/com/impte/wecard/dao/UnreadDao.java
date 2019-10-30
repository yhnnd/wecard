package com.impte.wecard.dao;

import com.impte.wecard.domain.po.Unread;
import org.springframework.stereotype.Repository;

@Repository
public interface UnreadDao extends GenericDao<Unread, String> {
    int insertUnread(Unread unread);
    int findUnreadCount(String userId);
    int deleteByChatItemId(String chatItemId);
}
