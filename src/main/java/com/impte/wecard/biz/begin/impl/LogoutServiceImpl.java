package com.impte.wecard.biz.begin.impl;

import com.impte.wecard.biz.begin.LogoutService;
import com.impte.wecard.dao.LogDao;
import com.impte.wecard.dao.UserDao;
import com.impte.wecard.domain.po.Log;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.Address;
import com.impte.wecard.utils.CurrentUtil;
import com.impte.wecard.utils.IP;
import com.impte.wecard.utils.UUID;
import com.impte.wecard.utils.websocket.OnlineSession;
import com.impte.wecard.utils.websocket.WebSocket;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

@Service
@AllArgsConstructor
public class LogoutServiceImpl implements LogoutService {

    private final UserDao userDao;

    private final LogDao logDao;

    @Override
    public void httpLogout() {

        HttpServletRequest request = CurrentUtil.getRequest();
        HttpSession session = CurrentUtil.getSession();
        if (session != null){
            //获得session中的user
            User user = (User) session.getAttribute("user");
            //获取session中的地点
            String address = Address.getAddressByIP(IP.getClientIpAddress(request));

            if (user != null){
                String userId = user.getId();
                //更新登出时间
                userDao.setLogoutTime(userId);
                //移除session中的user
                session.removeAttribute("user");
                //移除这个用户的webSocket，断开webSocket连接
                Map<String, OnlineSession> webSockets = WebSocket.getWebSockets();
                Map<WebSocketSession, String> webSocketIdMap = WebSocket.getWebSocketIdMap();
                if (webSocketIdMap.size() > 0) {
                    WebSocketSession webSocketSession = webSockets.get(userId).getWebSocketSession();
                    webSocketIdMap.remove(webSocketSession);
                    webSockets.remove(userId);
                }
                //插入登出日志
                logDao.insert(
                        new Log(
                                UUID.getUUID(),
                                "退出登录",
                                address,
                                user
                        )
                );
                //移除session中的地址城市
                session.removeAttribute("login_address");
            }
        }
    }
}
