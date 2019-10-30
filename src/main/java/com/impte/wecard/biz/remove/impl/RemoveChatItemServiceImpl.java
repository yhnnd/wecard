package com.impte.wecard.biz.remove.impl;

import com.impte.wecard.biz.remove.RemoveChatItemService;
import com.impte.wecard.dao.ChatItemDao;
import com.impte.wecard.domain.po.ChatItem;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RemoveChatItemServiceImpl implements RemoveChatItemService {

    private final ChatItemDao chatItemDao;

    @Override
    public String removeChatItem(String chatItemId) {
        String message;
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (chatItemId == null || chatItemId.equals("")){
            message = "ChatItemId cannot be empty";
        }else {
            ChatItem chatItem = chatItemDao.verifyChatItem(chatItemId);

            if (chatItem == null || !chatItem.getUser().getId().equals(user.getId())){
                message = "ChatItem does not exist";
            }else {
                int result = chatItemDao.setVisible(0, chatItemId);
                if (result != 1){
                    message = "Remove fail";
                }else {
                    message = "Remove success";
                }
            }
        }

        return message;
    }
}
