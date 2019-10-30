package com.impte.wecard.controller;

import com.impte.wecard.biz.person.PersonImgService;
import com.impte.wecard.biz.person.PersonMsgService;
import com.impte.wecard.utils.RespUtil;
import java.io.IOException;
import java.util.Map;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin
@AllArgsConstructor
@RequestMapping("/personSet")
public class PersonSetController {

    private final PersonImgService personImgService;
    private final PersonMsgService personMsgService;

    /**
     * 设置头像(GET)
     * @param avatar 头像图片(MultipartFile)，支持 png,jpg,gif 格式
     * @return JSON
     * @throws IOException 头像上传异常
     */
    @PostMapping("/setAvatar")
    public Map<String, String> setAvatar(@RequestParam MultipartFile avatar) throws IOException {
        return personImgService.setAvatar(avatar);
    }

    /**
     * 设置个性图(GET)
     * @param styleImg 个性图(MultipartFile)，支持 png,jpg,gif 格式
     * @return JSON
     * @throws IOException 个性图上传异常
     */
    @PostMapping("/setStyleImg")
    public Map<String, String> setStyleImg(@RequestParam MultipartFile styleImg) throws IOException {
        return personImgService.setStyleImg(styleImg);
    }

    /**
     * 设置昵称(GET)
     * @param nickname 昵称
     * @return JSON
     */
    @GetMapping("/setNickname")
    public Map<String, String> setNickname(@RequestParam String nickname){
        return RespUtil.packMsg(personMsgService.setNickname(nickname));
    }

    /**
     * 设置个性签名(GET)
     * @param signature 个性签名
     * @return JSON
     */
    @GetMapping("/setSignature")
    public Map<String, String> setSignature(@RequestParam String signature){
        return RespUtil.packMsg(personMsgService.setSignature(signature));
    }

    /**
     * 设置性别(GET)
     * @param gender 性别，可以为 male, female
     * @return JSON
     */
    @GetMapping("/setGender")
    public Map<String, String> setGender(@RequestParam String gender){
        return RespUtil.packMsg(personMsgService.setGender(gender));
    }

    /**
     * 设置城市(GET)
     * @param city 城市，不超过50个字符
     * @return JSON
     */
    @GetMapping("/setCity")
    public Map<String, String> setCity(@RequestParam String city){
        return RespUtil.packMsg(personMsgService.setCity(city));
    }
}
