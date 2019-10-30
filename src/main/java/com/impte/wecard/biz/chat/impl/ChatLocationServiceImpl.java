package com.impte.wecard.biz.chat.impl;

import com.impte.wecard.biz.chat.ChatLocationService;
import com.impte.wecard.dao.ChatItemDao;
import com.impte.wecard.dao.UnreadDao;
import com.impte.wecard.domain.po.ChatItem;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import com.impte.wecard.utils.websocket.OnlineSession;
import com.impte.wecard.utils.websocket.WebSocket;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class ChatLocationServiceImpl implements ChatLocationService {

    private final ChatItemDao chatItemDao;

    private final UnreadDao unreadDao;

    @Override
    public String setChatLocation(String location) {
        String message;
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (location == null || location.equals("")){
            message = "Location cannot be empty";
        }else if ("external".equals(location)){
            Map<String, OnlineSession> webSockets = WebSocket.getWebSockets();
            OnlineSession onlineSession = webSockets.get(user.getId());
            if (onlineSession != null) {
                onlineSession.setLocation("external");
            }
            message = "Set success";
        }else {
                //验证location是否合法
                ChatItem chatItem = chatItemDao.verifyChatItem(location);
                if (chatItem == null || !chatItem.getUser().getId().equals(user.getId())){
                    message = "Location not valid";
                }else {
                    log.info("setChatLocation | 设置对应chatItem下的所有消息为已读（删除所有未读）");
                    //设置对应chatItem下的所有消息为已读（删除所有未读）
                    unreadDao.deleteByChatItemId(location);
                    Map<String, OnlineSession> webSockets = WebSocket.getWebSockets();
                    OnlineSession onlineSession = webSockets.get(user.getId());
                    onlineSession.setLocation(chatItem.getId());
                    message = "Set success";
                }
        }
        return message;
    }
}
