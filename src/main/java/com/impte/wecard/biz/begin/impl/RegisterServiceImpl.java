package com.impte.wecard.biz.begin.impl;

import com.impte.wecard.biz.begin.CheckFormatService;
import com.impte.wecard.biz.begin.RegisterService;
import com.impte.wecard.biz.begin.ShortMsgService;
import com.impte.wecard.biz.tool.impl.SetExpiredVerCode;
import com.impte.wecard.dao.ChatItemDao;
import com.impte.wecard.dao.GroupDao;
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
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Service
public class RegisterServiceImpl implements RegisterService {

    @Value("${wecard.shortMessage.register}")
    private String registerMsg;

    private final UserDao userDao;
    private final CheckFormatService checkFormatService;
    private final ShortMsgService shortMsgService;
    private final LogDao logDao;
    private final ChatItemDao chatItemDao;
    private final DataSourceTransactionManager txManager;
    private final GroupDao groupDao;

    public RegisterServiceImpl(UserDao userDao, CheckFormatService checkFormatService,
                               ShortMsgService shortMsgService, LogDao logDao, ChatItemDao chatItemDao,
                               DataSourceTransactionManager txManager, GroupDao groupDao) {
        this.userDao = userDao;
        this.checkFormatService = checkFormatService;
        this.shortMsgService = shortMsgService;
        this.logDao = logDao;
        this.chatItemDao = chatItemDao;
        this.txManager = txManager;
        this.groupDao = groupDao;
    }

    @Override
    public String sendVerCode(String phoneNumber) {
        String result;
        HttpServletRequest request = CurrentUtil.getRequest();
        String checkResult = checkFormatService.checkPhoneNumber(phoneNumber);
        if (!checkResult.equals("Phone number is available")){
            result = checkResult;
        }else{
            //1--随机生成五位数验证码
            String verCode = VcodeUtil.getRandomNumber(5);
            //2--生成短信
            String message = String.format(registerMsg, verCode);
            //3--发送短信
            result = shortMsgService.sendMessage(phoneNumber, message);

            if (result.equals("Send success")){//如果发送成功
                //4--将验证码和电话存入session
                HttpSession session = request.getSession();
                session.setAttribute("registerVerCode", MD5.getMD5(verCode));
                session.setAttribute("registerPhoneNumber", phoneNumber);

                //5--3分钟后擦除session中的VerCode
                Timer timer = new Timer();
                timer.schedule(
                        new SetExpiredVerCode(request.getSession(), "registerVerCode"),
                        180 * 1000
                );
            }
        }
        return result;
    }

    @Override
    public String registerService(String username, String password, String phone, String verCode) {

        String message;
        HttpServletRequest request = CurrentUtil.getRequest();
        HttpSession session = request.getSession();

        String usernameResult = checkFormatService.checkUsername(username);
        String phoneResult = checkFormatService.checkPhoneNumber(phone);
        String registerVerCode = (String) session.getAttribute("registerVerCode");
        String registerPhoneNumber = (String) session.getAttribute("registerPhoneNumber");

        if (!usernameResult.equals("Username is available")){
            message = usernameResult;
        }else if (!phoneResult.equals("Phone number is available")){
            message = phoneResult;
        }else if (password == null || password.equals("")) {
            message = "Password cannot be empty";
        }else if (verCode == null || verCode.equals("")){
            message = "Verification code cannot be empty";
        }else if (registerVerCode == null){
            message = "Verification code have not sent";
        }else if (registerVerCode.equals("expired")){
            message = "Verification code expired";
        }else if(!registerVerCode.equals(MD5.getMD5(verCode))){
            message = "Verification code error";
        }else if (!registerPhoneNumber.equals(phone)){
            message = "Phone numbers are inconsistent";
        }else if (password.length() < 8){
            message = "Password is too short";
        }else if (password.length() > 32){
            message = "Password is too long";
        }else if (passwordContainSpecialChar(password)){
            message = "Password contains special char";
        }else{
            //根据IP找到注册地址设置为默认地址，同时插入日志
            String city = Address.getAddressByIP(IP.getClientIpAddress(request));
            //插入user
            String userId = UUID.getUUID();
            User user = new User(
                    userId,
                    username,
                    MD5.getMD5(password),
                    phone,
                    username,//默认将你nickname设置为username
                    city
            );

            //..................................................插入相关表....................................................
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);// 事物隔离级别，开启新事务
            TransactionStatus status = txManager.getTransaction(def);// 获得事务状态
            try{
                //插入用户
                userDao.registerUser(user);
                //插入系统chatItem
                chatItemDao.insertSystemChatItem(UUID.getUUID(), userId);
                //插入请求通知chatItem
                chatItemDao.insertInformChatItem(UUID.getUUID(), userId);
                //新用户添加默认分组
                groupDao.insertGroup(UUID.getUUID(), "我的好友", userId);

                //插入注册日志
                logDao.insert(
                        new Log(
                                UUID.getUUID(),
                                "注册",
                                city,
                                user
                        )
                );
                //设置message为success注册成功,提交事务
                message = "Register success";
                txManager.commit(status);
            }catch(Exception e){
                //设置message为fail注册失败,事务回滚
                message = "Register fail";
                txManager.rollback(status);
            }

            //擦除session中的验证码和注册电话
            session.removeAttribute("registerVerCode");
            session.removeAttribute("registerPhoneNumber");
        }
        return message;
    }

    public boolean passwordContainSpecialChar(String str) {
        String regEx = "[ @#$%^&*()+=|{}':;'\\[\\]/@#￥%……&*（）——+|{}【】‘；：”“’]|\n|\r|\t";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.find();
    }
}