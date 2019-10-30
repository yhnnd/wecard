package com.impte.wecard.controller;

import com.impte.wecard.biz.begin.ForgetPwdService;
import com.impte.wecard.biz.begin.LoginService;
import com.impte.wecard.biz.begin.LogoutService;
import com.impte.wecard.biz.begin.RefreshService;
import com.impte.wecard.utils.RespUtil;
import java.util.Map;

import com.impte.wecard.utils.web.ResponseMessage;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@AllArgsConstructor
public class LoginController {

    private final LoginService loginService;
    private final LogoutService logoutService;
    private final ForgetPwdService forgetPwdService;
    private final RefreshService refreshService;

    /**
     * 登录(GET)
     * @param username 验证名，可以是用户名或者电话
     * @param password 用户密码
     * @return JSON
     */
    @GetMapping("/login")
    public Map<String, Object> verifyLogin(@RequestParam String username, @RequestParam String password) {
        return loginService.loginService(username, password);
    }

    /**
     * 刷新聊天相关信息(GET)
     * @return JSON
     */
    @GetMapping("/refresh")
    public Map<String, Object> refresh() {
        return refreshService.refresh();
    }

    /**
     * 忘记密码第一步，验证账户(GET)
     * @param account 账号名，可以是用户名或者电话
     * @return JSON
     */
    @GetMapping("/login/forgetPwd/confirmAccount")
    public Map<String, String> confirmAccount(@RequestParam String account){
        return forgetPwdService.confirmAccount(account);
    }

    /**
     *忘记密码第二步，发送验证码(GET)
     * @return JSON
     */
    @GetMapping("/login/forgetPwd/sendVerCode")
    public Map<String, String> sendVerCode(){
        return RespUtil.packMsg(forgetPwdService.sendVerCode());
    }

    /**
     * 忘记密码第三步，提交验证验证码(GET)
     * @param verCode 验证码
     * @return JSON
     */
    @GetMapping("/login/forgetPwd/verifyVerCode")
    public Map<String, String> verifyVerCode(@RequestParam String verCode){
        return RespUtil.packMsg(forgetPwdService.verifyVerCode(verCode));
    }

    /**
     * 忘记密码第四步，修改密码(GET)
     * @param password 新密码
     * @return JSON
     */
    @GetMapping("/login/forgetPwd/setPassword")
    public Map<String, String> setPassword(@RequestParam String password){
        return RespUtil.packMsg(forgetPwdService.setPassword(password));
    }

    /**
     * 登出(GET)
     */
    @GetMapping("/logout")
    public void logout(){
        logoutService.httpLogout();
    }

    /**
     * 返回用户未登录提示
     * @return JSON
     */
    @GetMapping("/login/unLogin")
    public Map<String, String> unLogin() {
        return RespUtil.packMsg(ResponseMessage.NOT_LOGIN);
    }
}
