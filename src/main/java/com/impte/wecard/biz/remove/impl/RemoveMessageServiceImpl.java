package com.impte.wecard.biz.remove.impl;

import com.impte.wecard.biz.remove.RemoveMessageService;
import com.impte.wecard.dao.MessageDao;
import com.impte.wecard.domain.po.Message;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import com.impte.wecard.utils.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RemoveMessageServiceImpl implements RemoveMessageService {

    private final MessageDao messageDao;

    @Override
    public String removeFriendMessage(String messageId) {
        String result;
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (messageId == null || messageId.equals("")){
            result = "MessageId cannot be empty";
        }else {
            String userId = user.getId();
            //验证用户是否有权利删除这条消息，验证这条消息对于当前用户是否有资格浏览
            Message message = messageDao.verifyFriendMsgBelong(messageId, userId);
            //验证消息是否已经被删除
            String removeResult = messageDao.findRemovedMessage(userId, messageId);
            if (message == null){
                result = "Message does not exist";
            }else if (removeResult != null){
                result = "Message has been removed";
            }else {
                //将消息插入删除消息表，表示某个用户删除某条信息
                int insertResult = messageDao.insertRemoveMessage(UUID.getUUID(), userId, messageId);
                if (insertResult == 0){
                    result = "Remove fail";
                }else {
                    result = "Remove success";
                }
            }
        }

        return result;
    }

    @Override
    public String removeRoomMessage(String messageId) {
        String result;
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (messageId == null || messageId.equals("")){
            result = "MessageId cannot be empty";
        }else {
            String userId = user.getId();
            //验证用户是否有权利删除这条消息，验证这条消息对于当前用户是否有资格浏览
            Message message = messageDao.verifyRoomMsgBelong(messageId, userId);
            //验证消息是否已经被删除
            String removeResult = messageDao.findRemovedMessage(userId, messageId);
            if (message == null){
                result = "Message does not exist";
            }else if (removeResult != null){
                result = "Message has been removed";
            }else {
                //将消息插入删除消息表，表示某个用户删除某条信息
                int insertResult = messageDao.insertRemoveMessage(UUID.getUUID(), userId, messageId);
                if (insertResult == 0){
                    result = "Remove fail";
                }else {
                    result = "Remove success";
                }
            }
        }

        return result;
    }
}
