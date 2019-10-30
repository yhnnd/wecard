package com.impte.wecard.biz.begin.impl;

import com.impte.wecard.biz.Constant;
import com.impte.wecard.biz.begin.ForgetPwdService;
import com.impte.wecard.biz.begin.ShortMsgService;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author xutong
 */
@Service
public class ForgetPwdServiceImpl implements ForgetPwdService {

    @Value("${wecard.shortMessage.forgetPwd}")
    private String forgetPwdMsg;

    private final UserDao userDao;
    private final ShortMsgService shortMsgService;
    private final LogDao logDao;

    public ForgetPwdServiceImpl(UserDao userDao, ShortMsgService shortMsgService, LogDao logDao) {
        this.userDao = userDao;
        this.shortMsgService = shortMsgService;
        this.logDao = logDao;
    }

    @Override
    public Map<String, String> confirmAccount(String account) {

        HttpServletRequest request = CurrentUtil.getRequest();
        User user = userDao.findPhoneByAccount(account);
        Map<String, String> map = new HashMap<>();
        if (user == null){
            map.put(Constant.MESSAGE, Constant.THE_ACCOUNT_DOES_NOT_EXIST);
        }else{
            String phoneNumber = user.getPhone();
            if (phoneNumber == null || phoneNumber.equals("") || phoneNumber.length() != 11){
                map.put("message", "Confirm fail");
            }else {
                map.put("message", "Confirm success");
                map.put("coverUpPhone", coverUpPhone(phoneNumber));

                request.getSession().setAttribute("forgetPwd_phoneNumber", user.getPhone());
                request.getSession().setAttribute("forgetPwd_username", user.getUsername());
                request.getSession().setAttribute("forgetPwdId", user.getId());
            }
        }

        return map;
    }

    @Override
    public String sendVerCode() {

        String result;
        //1--从session中获取电话和用户名
        HttpServletRequest request = CurrentUtil.getRequest();
        HttpSession session = CurrentUtil.getSession();
        String phoneNumber = (String) session.getAttribute("forgetPwd_phoneNumber");
        String username = (String) session.getAttribute("forgetPwd_username");

        if (phoneNumber == null || phoneNumber.equals("") || username == null || username.equals("")){
            result = "Send fail";
        }else {
            //1--生成5位验证码
            String verCode = VcodeUtil.getRandomNumber(5);
            //2--找到修改密码的地址
            String address = Address.getAddressByIP(IP.getClientIpAddress(request));
            //3--生成短信
            String message = String.format(forgetPwdMsg, username, address, verCode);
            //4--发送短信
            result = shortMsgService.sendMessage(phoneNumber, message);

            if (result.equals("Send success")){//如果发送成功
                //5--将验证码存入session
                session.setAttribute("forgetPwdVerCode", MD5.getMD5(String.valueOf(verCode)));

                //6--3分钟后擦除session中的forgetPwdVerCode
                Timer timer = new Timer();
                timer.schedule(
                        new SetExpiredVerCode(request.getSession(), "forgetPwdVerCode"),
                        180 * 1000
                );
            }
        }

        return result;
    }

    @Override
    public String verifyVerCode(String verCode) {
        String message;

        if (verCode == null || "".equals(verCode)){
            message = "Verification code cannot be empty";
        }else {
            HttpSession session = CurrentUtil.getSession();
            String forgetPwdVerCode = (String) session.getAttribute("forgetPwdVerCode");

            if (session.getAttribute("forgetPwdVerCode") == null){
                message = "Verification code has not sent";
            }else if ("expired".equals(forgetPwdVerCode)){
                message = "Verification code expired";
            }else if(!forgetPwdVerCode.equals(MD5.getMD5(verCode))){
                message = "Verification code error";
            }else{
                session.removeAttribute("forgetPwdVerCode");
                session.removeAttribute("forgetPwd_username");
                session.removeAttribute("forgetPwd_phoneNumber");
                session.setAttribute("verifyResult", "success");
                message = "Verify success";
            }
        }
        return message;
    }

    @Override
    public String setPassword(String password) {
        String message;

        HttpServletRequest request = CurrentUtil.getRequest();
        HttpSession session = CurrentUtil.getSession();
        String userId = (String) session.getAttribute("forgetPwdId");
        String verifyResult = (String) session.getAttribute("verifyResult");
        if (!"success".equals(verifyResult) || userId == null || ""
            .equals(userId)){
            session.removeAttribute("verifyResult");
            session.removeAttribute("forgetPwdId");
            message = "Set fail";
        }else if (password == null || password.equals("")) {
            message = "Password cannot be empty";
        }else if (password.length() < 8){
            message = "Password is too short";
        }else if (passwordContainSpecialChar(password)){
            message = "Password contains special char";
        }else{
            int result = userDao.alterPassword(userId, MD5.getMD5(password));
            if (result == 1){
                session.removeAttribute("verifyResult");
                session.removeAttribute("forgetPwdId");
                message = "Set success";

                logDao.insert(
                        new Log(
                                UUID.getUUID(),
                                "手机短信验证，找回用户密码",
                                Address.getAddressByIP(IP.getClientIpAddress(request)),
                                new User(userId)
                        )
                );
            }else {
                session.removeAttribute("verifyResult");
                session.removeAttribute("forgetPwdId");
                message = "Set fail";
            }
        }
        return message;
    }

    //掩盖号码中间四位
    private String coverUpPhone(String mobile){
        String begin = mobile.substring(0, 3);
        String end = mobile.substring(7);
        return begin + "****" + end;
    }

    public boolean passwordContainSpecialChar(String str) {
        String regEx = "[ @#$%^&*()+=|{}':;'\\[\\]/@#￥%……&*（）——+|{}【】‘；：”“’]|\n|\r|\t";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.find();
    }
}