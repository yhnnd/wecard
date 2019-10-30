package com.impte.wecard.biz.find.impl;

import com.impte.wecard.biz.find.FindChatItemService;
import com.impte.wecard.dao.ChatItemDao;
import com.impte.wecard.domain.po.ChatItem;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FindChatItemServiceImpl implements FindChatItemService {

    private final ChatItemDao chatItemDao;

    @Override
    public Map<String, Object> findChatItem(String chatItemId) {
        String message;
        Map<String, Object> map = new HashMap<>();
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (chatItemId == null || chatItemId.equals("")){
            message = "ChatItemId cannot be empty";
        }else {
            ChatItem chatItem = chatItemDao.findById(chatItemId);
            if (chatItem == null){
                message = "ChatItem does not exist";
            }else if (!chatItem.getUser().getId().equals(user.getId())){
                message = "ChatItem permission error";
            }else {
                chatItem.setUser(null);
                map.put("chatItem", chatItem);
                message = "Find success";
            }
        }
        map.put("message", message);

        return map;
    }
}
