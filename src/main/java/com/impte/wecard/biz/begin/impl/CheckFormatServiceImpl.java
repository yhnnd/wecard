package com.impte.wecard.biz.begin.impl;

import com.impte.wecard.biz.Constant;
import com.impte.wecard.biz.begin.CheckFormatService;
import com.impte.wecard.dao.UserDao;
import com.impte.wecard.utils.Phone;
import com.impte.wecard.utils.SpecialChar;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author xutong
 */
@Service
@AllArgsConstructor
public class CheckFormatServiceImpl implements CheckFormatService {

    private final UserDao userDao;

    @Override
    public String checkUsername(String username) {
        if (username == null || "".equals(username)){
            return Constant.USERNAME_CANNOT_BE_EMPTY;
        }else if (username.length() > Constant.MAX_USERNAME_LENGTH){
            return Constant.USERNAME_IS_TOO_LONG;
        }else if (SpecialChar.usernameContainSpecialChar(username)){
            return Constant.USERNAME_CONTAINS_SPECIAL_CHAR;
        }else if (userDao.checkUserName(username) != null){
            return Constant.USERNAME_ALREADY_EXISTS;
        }else {
            return Constant.USERNAME_IS_AVAILABLE;
        }
    }

    @Override
    public String checkPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || "".equals(phoneNumber)){
            return Constant.PHONE_NUMBER_CANNOT_BE_EMPTY;
        }else if (!Phone.checkMobileNumber(phoneNumber)){
            return Constant.INCORRECT_PHONE_NUMBER;
        }else if (userDao.checkPhoneNumber(phoneNumber) != null){
            return Constant.PHONE_NUMBER_ALREADY_EXISTS;
        }else {
            return Constant.PHONE_NUMBER_IS_AVAILABLE;
        }
    }
}
