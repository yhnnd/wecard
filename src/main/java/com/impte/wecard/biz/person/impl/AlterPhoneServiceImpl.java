package com.impte.wecard.biz.person.impl;

import com.impte.wecard.biz.begin.CheckFormatService;
import com.impte.wecard.biz.begin.ShortMsgService;
import com.impte.wecard.biz.person.AlterPhoneService;
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
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class  AlterPhoneServiceImpl implements AlterPhoneService {

    @Value("${wecard.shortMessage.alterPhone}")
    private String alterMsg;

    private final ShortMsgService shortMsgService;
    private final UserDao userDao;
    private final CheckFormatService checkFormatService;
    private final LogDao logDao;

    public AlterPhoneServiceImpl(ShortMsgService shortMsgService, UserDao userDao,
                                 CheckFormatService checkFormatService, LogDao logDao) {
        this.shortMsgService = shortMsgService;
        this.userDao = userDao;
        this.checkFormatService = checkFormatService;
        this.logDao = logDao;
    }

    @Override
    public String sendVerCode(String newPhoneNumber) {

        String message;
        String verifyResult = checkFormatService.checkPhoneNumber(newPhoneNumber);
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权
        if (!"Phone number is available".equals(verifyResult)){
            message = verifyResult;
        }else {
            String username = user.getUsername();
            if (username == null || username.equals("")){
                message = "Send fail";
            }else {
                //1--生成5位验证码
                String verCode = VcodeUtil.getRandomNumber(5);
                String shortMsg = String.format(alterMsg, username, verCode);
                //4--发送短信
                message = shortMsgService.sendMessage(newPhoneNumber, shortMsg);
                if (message.equals("Send success")){
                    HttpSession session = CurrentUtil.getSession();
                    session.setAttribute("alterPhone_verCode", MD5.getMD5(String.valueOf(verCode)));
                    session.setAttribute("alterPhone_Number", newPhoneNumber);
                    Timer timer = new Timer();
                    timer.schedule(
                            new SetExpiredVerCode(session, "alterPhone_verCode"),
                            180 * 1000
                    );
                }
            }
        }

        return message;
    }

    @Override
    public String alterPhone(String verCode) {
        String message;

        HttpSession session = CurrentUtil.getSession();

        String alterPhoneVerCode = (String) session.getAttribute("alterPhone_verCode");
        String newPhoneNumber = (String) session.getAttribute("alterPhone_Number");

        // 已经过登录状态检查拦截器鉴权

        if (verCode == null || verCode.equals("")){
            message = "Verification code cannot be empty";
        }else if (alterPhoneVerCode == null){
            message = "Verification code have not sent";
        }else if (alterPhoneVerCode.equals("expired")){
            message = "Verification code expired";
        }else if (!alterPhoneVerCode.equals(MD5.getMD5(verCode))){
            message = "Verification code error";
        }else {
            User user = CurrentUtil.getUser();
            String userId = user.getId();
            if (userId == null || userId.equals("") || newPhoneNumber == null){
                message = "Alter fail";
                session.removeAttribute("alterPhone_verCode");
                session.removeAttribute("alterPhone_Number");
            }else {
                //先取出老手机号，以便插入日志使用
                String oldPhone = user.getPhone();
                //更新手机号码
                int result = userDao.alterPhone(userId, newPhoneNumber);

                if (result == 1){
                    message = "Alter success";
                    session.removeAttribute("alterPhone_verCode");
                    session.removeAttribute("alterPhone_Number");

                    //插入改绑手机日志
                    String description = "通过手机短信验证，修改绑定手机的号码，老号码是："+ oldPhone +"，新号码是：" + newPhoneNumber;
                    logDao.insert(
                            new Log(
                                    UUID.getUUID(),
                                    description,
                                    Address.getAddressByIP(IP.getClientIpAddress(
                                        Objects.requireNonNull(CurrentUtil.getRequest()))),
                                    user
                            )
                    );
                }else {
                    message = "Alter fail";
                    session.removeAttribute("alterPhone_verCode");
                    session.removeAttribute("alterPhone_Number");
                }
            }
        }
        return message;
    }
}
