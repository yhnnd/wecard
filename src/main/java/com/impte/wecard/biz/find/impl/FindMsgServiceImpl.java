package com.impte.wecard.biz.find.impl;

import com.impte.wecard.biz.find.FindMsgService;
import com.impte.wecard.dao.MessageDao;
import com.impte.wecard.domain.po.Message;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpSession;

import com.impte.wecard.utils.web.ResponseMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FindMsgServiceImpl implements FindMsgService {

    private final MessageDao messageDao;

    @Override
    public Map<String, Object> findMessage(String messageId) {
        String result;
        Map<String, Object> map = new HashMap<>();
        User user = CurrentUtil.getUser();

        if (user==null || user.getId()==null) {
            result = ResponseMessage.NOT_LOGIN;
        } else if (messageId == null || messageId.equals("")){
            result = "MessageId cannot be empty";
        }else {
            String userId = user.getId();
            Message message = null;
            String roomResult = messageDao.verifyTypeRoom(messageId);
            String friendResult = messageDao.verifyTypeFriend(messageId);
            if (roomResult != null){
                message = messageDao.findRoomMsgItem(messageId, userId);
            }else if (friendResult != null){
                message = messageDao.findFriendMsgItem(messageId, userId);
            }
            if (message == null){//----------------------------------------------------需要添加权限验证！
                result = "Message does not exist";
            }else {
                map.put("message", message);
                result = "Find success";
            }
        }
        map.put("result", result);

        return map;
    }
}
