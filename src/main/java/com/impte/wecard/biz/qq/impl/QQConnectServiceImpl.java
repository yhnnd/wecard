package com.impte.wecard.biz.qq.impl;

import com.impte.wecard.biz.Constant;
import com.impte.wecard.biz.begin.LoginService;
import com.impte.wecard.biz.qq.NewQQUserService;
import com.impte.wecard.biz.qq.QQConnectService;
import com.impte.wecard.dao.UserDao;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.Address;
import com.impte.wecard.utils.IP;
import com.impte.wecard.utils.UUID;
import com.qq.connect.QQConnectException;
import com.qq.connect.api.OpenID;
import com.qq.connect.api.qzone.UserInfo;
import com.qq.connect.javabeans.AccessToken;
import com.qq.connect.javabeans.qzone.UserInfoBean;
import com.qq.connect.oauth.Oauth;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class QQConnectServiceImpl implements QQConnectService {

    private final NewQQUserService newQQUserService;
    private final UserDao userDao;
    private final LoginService loginService;

    @Override
    public void QQConnect(HttpServletRequest request) {

        HttpSession session = request.getSession();
        try {
            AccessToken accessTokenObj = (new Oauth()).getAccessTokenByRequest(request);

            String accessToken, openID;
            long tokenExpireIn;

            if (accessTokenObj.getAccessToken().equals("")) {
                System.out.print("没有获取到响应参数");
            } else {
                accessToken = accessTokenObj.getAccessToken();
                tokenExpireIn = accessTokenObj.getExpireIn();

                request.getSession().setAttribute("demo_access_token", accessToken);
                request.getSession().setAttribute("demo_token_expirein", String.valueOf(tokenExpireIn));

                // 利用获取到的accessToken 去获取当前用的openid -------- start
                OpenID openIDObj =  new OpenID(accessToken);
                openID = openIDObj.getUserOpenID();

                UserInfo qzoneUserInfo = new UserInfo(accessToken, openID);
                UserInfoBean userInfoBean = qzoneUserInfo.getUserInfo();
                if (userInfoBean.getRet() == 0) {
                    String gender;
                    switch (userInfoBean.getGender()) {
                        case "男":
                            gender = "male";
                            break;
                        case "女":
                            gender = "female";
                            break;
                        default:
                            gender = "unknown";
                            break;
                    }

                    String username = UUID.changeBand16to64(openID);
                    User dataUser = userDao.verifyUserExistByUsername(username);
                    if (dataUser != null){
                        User user = userDao.loginGetUser(dataUser.getId());
                        session.setAttribute(Constant.USER, user);
                    }else {
                        String city = Address.getAddressByIP(IP.getClientIpAddress(request));
                        String userId = UUID.getUUID();
                        newQQUserService.NewQQUser(
                                userId,
                                username,
                                gender,
                                userInfoBean.getNickname(),
                                userInfoBean.getAvatar().getAvatarURL100(),
                                city
                        );

                        loginService.login(userId, session);
                    }
                }
            }
        } catch (QQConnectException ignored) {}
    }
}
