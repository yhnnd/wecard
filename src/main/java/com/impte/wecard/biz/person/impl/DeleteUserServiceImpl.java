package com.impte.wecard.biz.person.impl;

import com.impte.wecard.biz.begin.ShortMsgService;
import com.impte.wecard.biz.person.DeleteUserService;
import com.impte.wecard.biz.tool.impl.SetExpiredVerCode;
import com.impte.wecard.dao.LogDao;
import com.impte.wecard.dao.UserDao;
import com.impte.wecard.domain.po.Log;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.Address;
import com.impte.wecard.utils.CurrentUtil;
import com.impte.wecard.utils.IP;
import com.impte.wecard.utils.MD5;
import com.impte.wecard.utils.UUID;
import com.impte.wecard.utils.VcodeUtil;
import java.util.Objects;
import java.util.Timer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DeleteUserServiceImpl implements DeleteUserService {

    @Value("${wecard.shortMessage.deleteUser}")
    private String deleteUserMsg;

    private final ShortMsgService shortMsgService;
    private final UserDao userDao;
    private final LogDao logDao;

    public DeleteUserServiceImpl(ShortMsgService shortMsgService, UserDao userDao, LogDao logDao) {
        this.shortMsgService = shortMsgService;
        this.userDao = userDao;
        this.logDao = logDao;
    }

    @Override
    public String sendVerCode() {
        String message;

        User user = CurrentUtil.getUser();
        HttpSession session = CurrentUtil.getSession();

        // 已经过登录状态检查拦截器鉴权

        String phoneNumber = user.getPhone();
        String username = user.getUsername();

        String verCode = VcodeUtil.getRandomNumber(5);
        if (phoneNumber == null || phoneNumber.equals("") || username == null || username.equals("")){
            message = "Send fail";
        }else {
            String shortMsg = String.format(deleteUserMsg, username, verCode);
            message =  shortMsgService.sendMessage(phoneNumber, shortMsg);

            if (message.equals("Send success")){
                session.setAttribute("deleteUser_verCode", MD5.getMD5(verCode));
                Timer timer = new Timer();
                timer.schedule(
                        new SetExpiredVerCode(session, "deleteUserVerCode"),
                        180 * 1000
                );
            }
        }

        return message;
    }

    @Override
    public String deleteUser(String verCode) {
        String message;


        User user = CurrentUtil.getUser();
        HttpSession session = CurrentUtil.getSession();
        String deleteUserVerCode = (String) session.getAttribute("deleteUserVerCode");

        // 已经过登录状态检查拦截器鉴权

        if (verCode == null || verCode.equals("")){
            message = "Verification code cannot be empty";
        }else if (deleteUserVerCode == null){
            message = "Verification code have not sent";
        }else if ("expired".equals(deleteUserVerCode)){
            message = "Verification code expired";
        }else if (!deleteUserVerCode.equals(MD5.getMD5(verCode))){
            message = "Verification code error";
        }else {
            String userId = user.getId();
            if (userId == null || userId.equals("")){
                message = "Delete fail";
                session.removeAttribute("deleteUserVerCode");
                session.removeAttribute("user");
            }else {
                int result = userDao.deleteUser(userId);

                if (result == 1){
                    message = "Delete success";
                    session.removeAttribute("deleteUserVerCode");
                    session.removeAttribute("user");

                    //插入账户注销（删除）日志
                    logDao.insert(
                            new Log(
                                    UUID.getUUID(),
                                    "通过手机短信验证，注销（删除）用户",
                                    Address.getAddressByIP(IP.getClientIpAddress(
                                        Objects.requireNonNull(CurrentUtil.getRequest()))),
                                    user
                            )
                    );
                }else {
                    message = "Delete fail";
                    session.removeAttribute("deleteUserVerCode");
                    session.removeAttribute("user");
                }
            }
        }

        return message;
    }
}
