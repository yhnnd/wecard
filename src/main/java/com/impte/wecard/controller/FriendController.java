package com.impte.wecard.controller;

import com.impte.wecard.biz.create.SetRemarkService;
import com.impte.wecard.biz.friend.AgreeFriendService;
import com.impte.wecard.biz.friend.RequestService;
import com.impte.wecard.utils.RespUtil;
import java.util.Map;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@AllArgsConstructor
@RequestMapping("/friend")
public class FriendController {

    private final RequestService requestService;
    private final AgreeFriendService agreeFriendService;
    private final SetRemarkService setRemarkService;

    /**
     * 发送加好友的请求(GET)
     * @param username 需要加为好友用户的用户名或者电话
     * @param groupId 需要将好友存放在现有的某一个分组的id
     * @param remark 给这个好友加的备注
     * @param requestMessage 验证消息
     * @return JSON
     */
    @GetMapping("/makeFriendRequest")
    public Map<String, String> makeFriendRequest(
            @RequestParam String username,
            @RequestParam String groupId,
            @RequestParam String remark,
            @RequestParam(required = false, defaultValue = "") String requestMessage
            ){
        return RespUtil.packMsg(requestService.makeFriendRequest(username, groupId, remark, requestMessage));
    }

    /**
     * 同意加好友的请求(GET)
     * @param requestId 加好友请求的id
     * @param groupId 接受方打算把这个请求者分到的分组的id
     * @param remark 接受者给请求者设置的备注
     * @return JSON
     */
    @GetMapping("/agreeMakeFriendRequest")
    public Map<String, String> agreeMakeFriendRequest(
            @RequestParam String requestId,
            @RequestParam String groupId,
            @RequestParam String remark
            ){
        return RespUtil.packMsg(agreeFriendService.agreeFriendRequest(groupId, remark, requestId));
    }

    /**
     * 给好友设置备注(GET)
     * @param friendId 朋友的id
     * @param remark 新的备注
     * @return JSON
     */
    @GetMapping("/set/remark")
    public Map<String, String> setRemark(
            @RequestParam String friendId,
            @RequestParam String remark
            ){
        return RespUtil.packMsg(setRemarkService.setFriendRemark(remark, friendId));
    }
}
