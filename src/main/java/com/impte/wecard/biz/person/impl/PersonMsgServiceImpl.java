package com.impte.wecard.biz.person.impl;

import com.impte.wecard.biz.person.PersonMsgService;
import com.impte.wecard.dao.LogDao;
import com.impte.wecard.dao.UserDao;
import com.impte.wecard.domain.po.Log;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.Address;
import com.impte.wecard.utils.CurrentUtil;
import com.impte.wecard.utils.IP;
import com.impte.wecard.utils.SpecialChar;
import com.impte.wecard.utils.UUID;

import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PersonMsgServiceImpl implements PersonMsgService {

    private final UserDao userDao;
    private final LogDao logDao;

    @Override
    public String setNickname(String nickname) {
        String message;
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (nickname == null || nickname.equals("")){
            message = "Nickname cannot be empty";
        }else if (SpecialChar.nicknameContainSpecialChar(nickname)){
            message = "Nickname contains special char";
        }else if (nickname.length() > 12){
            message = "Nickname is too long";
        }else {
            int result = userDao.setNickname(user.getId(), nickname);

            if (result == 1){
                message = "Set success";

                //插入修改昵称日志
                logDao.insert(
                        new Log(
                                UUID.getUUID(),
                                "修改昵称",
                                Address.getAddressByIP(IP.getClientIpAddress(
                                    Objects.requireNonNull(CurrentUtil.getRequest()))),
                                user
                        )
                );
            }else{
                message = "Set fail";
            }
        }
        return message;
    }

    @Override
    public String setSignature(String signature) {
        String message;
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (signature == null || signature.equals("")){
            message = "Signature cannot be empty";
        }else if (signature.length() > 100){
            message = "Signature is too long";
        }else {
            //过滤掉所有的网页元素，过滤掉空白符，制表符等，并插入
            int result = userDao.setSignature(user.getId(), signature);

            if (result == 1){
                message = "Set success";

                //插入修改个性签名日志
                logDao.insert(
                        new Log(
                                UUID.getUUID(),
                                "修改个性签名",
                                Address.getAddressByIP(IP.getClientIpAddress(
                                Objects.requireNonNull(CurrentUtil.getRequest()))),
                                user
                        )
                );
            }else{
                message = "Set fail";
            }
        }
        return message;
    }

    @Override
    public String setGender(String gender) {
        String message;
        User user = CurrentUtil.getUser();
        // 已经过登录状态检查拦截器鉴权

        if (gender == null || gender.equals("")){
            message = "Gender cannot be empty";
        }else {
            if (gender.equals("male") || gender.equals("female") || gender.equals("neutral")){
                //过滤掉所有的网页元素，过滤掉空白符，制表符等，并插入
                int result = userDao.setGender(user.getId(), gender);

                if (result == 1){
                    message = "Set success";

                    //插入修改性别日志
                    logDao.insert(
                            new Log(
                                    UUID.getUUID(),
                                    "修改性别",
                                    Address.getAddressByIP(IP.getClientIpAddress(
                                    Objects.requireNonNull(CurrentUtil.getRequest()))),
                                    user
                            )
                    );
                }else{
                    message = "Set fail";
                }
            }else {
                message = "Unknown gender";
            }
        }
        return message;
    }

    @Override
    public String setCity(String city) {
        String message;
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (city == null || city.equals("")){
            message = "City cannot be empty";
        }else if (SpecialChar.ContainSpecialChar(city)){
            message = "City contains special char";
        }else if (city.length() > 50){
            message = "City is too long";
        }else {
            int result = userDao.setCity(user.getId(), city);

            if (result == 1){
                message = "Set success";

                //插入修改城市日志
                logDao.insert(
                        new Log(
                                UUID.getUUID(),
                                "修改城市",
                                Address.getAddressByIP(IP.getClientIpAddress(
                                Objects.requireNonNull(CurrentUtil.getRequest()))),
                                user
                        )
                );
            }else{
                message = "Set fail";
            }
        }
        return message;
    }
}
