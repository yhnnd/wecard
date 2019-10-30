package com.impte.wecard.controller;

import com.impte.wecard.biz.begin.CheckFormatService;
import com.impte.wecard.biz.begin.RegisterService;
import com.impte.wecard.utils.RespUtil;
import java.util.Map;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@AllArgsConstructor
public class RegisterController {

    private final RegisterService registerService;
    private final CheckFormatService checkFormatService;

    /**
     * 注册(GET)
     * @param username 用户名
     * @param password 密码
     * @param phoneNumber 电话
     * @param verCode 验证码
     * @return JSON
     */
    @GetMapping("/register")
    public Map<String, String> register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String phoneNumber,
            @RequestParam String verCode
            ) {
        return RespUtil.packMsg(registerService.registerService(username, password, phoneNumber,verCode));
    }

    /**
     * 验证码发送(GET)
     * @param phoneNumber 电话
     * @return JSON
     */
    @GetMapping("/register/sendVerCode")
    public Map<String, String> sendVerCode(@RequestParam String phoneNumber){
        return RespUtil.packMsg(registerService.sendVerCode(phoneNumber));
    }

    /**
     * 检查用户名是否被占用(GET)
     * @param username 用户名
     * @return JSON
     */
    @GetMapping("/register/checkUsername")
    public Map<String, String> checkUsername(@RequestParam String username){
        return RespUtil.packMsg(checkFormatService.checkUsername(username));
    }

    /**
     * 检查电话号码是否被占用(GET)
     * @param phoneNumber 电话
     * @return JSON
     */
    @GetMapping("/register/checkPhoneNumber")
    public Map<String, String> checkPhoneNumber(@RequestParam("phoneNumber") String phoneNumber){
        return RespUtil.packMsg(checkFormatService.checkPhoneNumber(phoneNumber));
    }
}
