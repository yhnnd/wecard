package com.impte.wecard.controller;

import com.impte.wecard.biz.begin.IsLoginService;
import com.impte.wecard.biz.person.AlterPhoneService;
import com.impte.wecard.biz.person.DeleteUserService;
import com.impte.wecard.utils.RespUtil;
import java.util.Map;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@AllArgsConstructor
@RequestMapping("/account")
public class AccountController {

    private final AlterPhoneService alterPhoneService;
    private final DeleteUserService deleteUserService;
    private final IsLoginService isLoginService;

    /**
     * 修改绑定的电话号码时，发送验证码(GET)
     * @param newPhoneNumber 新的电话号码
     * @return JSON
     */
    @GetMapping("/alterPhone/sendVerCode")
    public Map<String, String> sendAlterPhoneVerCode(@RequestParam String newPhoneNumber){
        return RespUtil.packMsg(alterPhoneService.sendVerCode(newPhoneNumber));
    }

    /**
     *修改绑定的电话号码时，验证码验证与修改电话号码(GET)
     * @param verCode 验证码
     * @return JSON
     */
    @GetMapping("/alterPhone")
    public Map<String, String> alterPhone(@RequestParam String verCode){
        return RespUtil.packMsg(alterPhoneService.alterPhone(verCode));
    }

    /**
     * 注销账号时，发送验证码给账户绑定的电话号码(GET)
     * @return JSON
     */
    @GetMapping("/deleteUser/sendVerCode")
    public Map<String, String> sendDeleteUserVerCode(){
        return RespUtil.packMsg(deleteUserService.sendVerCode());
    }

    /**
     * 注销账号时，验证码验证并删除账号(GET)
     * @param verCode 用于注销账号的验证码
     * @return JSON
     */
    @GetMapping("/deleteUser")
    public Map<String, String> deleteUser(@RequestParam String verCode){
        return RespUtil.packMsg(deleteUserService.deleteUser(verCode));
    }

    /**
     *用于判断账户是否登录，返回User(GET)
     * @return JSON
     */
    @GetMapping("/isLogin")
    public Map<String, Object> isLogin(){
        return isLoginService.isLogin();
    }
}
