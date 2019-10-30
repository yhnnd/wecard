package com.impte.wecard.controller;

import com.impte.wecard.biz.chat.ChatLocationService;
import com.impte.wecard.biz.chat.LoadMessagesService;
import com.impte.wecard.biz.chat.SendImgService;
import com.impte.wecard.biz.chat.SendMsgService;
import com.impte.wecard.utils.RespUtil;
import java.io.IOException;
import java.util.Map;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin
@AllArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final SendMsgService sendMsgService;

    private final ChatLocationService chatLocationService;

    private final LoadMessagesService loadMessagesService;

    private final SendImgService sendImgService;

    /**
     * 聊天时发送文本消息到消息体(GET)
     * @param chatItemId 消息体的id
     * @param text 发送的文本
     * @return JSON
     */
    @GetMapping("/sendMsgToChatItem")
    public Map<String, String> sendMsgToChatItem(
            @RequestParam String chatItemId,
            @RequestParam String text){
        return RespUtil.packMsg(sendMsgService.sendMsgToChatItem(chatItemId, text));
    }

    /**
     * 聊天时发送图片消息到消息体(POST)
     * @param image 图片(MultipartFile)
     * @param chatItemId 消息体的id
     * @return JSON
     * @throws IOException 图片上传异常
     */
    @PostMapping("/sendImgToChatItem")
    public Map<String, String> sendImgToChatItem(
            @RequestParam MultipartFile image,
            @RequestParam String chatItemId) throws IOException {
        return RespUtil.packMsg(sendImgService.sendImgToChatItem(chatItemId, image, "image"));
    }

    /**
     * 设置聊天位置，用户后台判断是否实时返回消息(GET)
     * @param location 用户操作位置，可以是external或者chatItemId
     * @return JSON
     */
    @GetMapping("/setChatLocation")
    public Map<String, String> setChatLocation(@RequestParam String location){
        return RespUtil.packMsg(chatLocationService.setChatLocation(location));
    }

    /**
     * 加载消息体中的消息，默认加载15条(GET)
     * @param chatItemId 消息体id
     * @return JSON
     */
    @GetMapping("/loadMessages")
    public Map<String, Object> loadMessages(@RequestParam String chatItemId){
        return loadMessagesService.loadMessages(chatItemId);
    }

    /**
     * 懒加载消息体中的消息(GET)
     * @param chatItemId 消息体id
     * @param offset 加载的起始点
     * @param limit 加载的条数
     * @return JSON
     */
    @GetMapping("/loadMessages/limit")
    public Map<String, Object> loadLimitMessages(
            @RequestParam String chatItemId,
            @RequestParam int offset,
            @RequestParam int limit
            ){
        return loadMessagesService.loadLimitMessages(chatItemId, offset, limit);
    }
}
