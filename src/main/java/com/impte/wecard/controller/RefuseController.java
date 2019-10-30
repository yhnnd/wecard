package com.impte.wecard.controller;

import com.impte.wecard.biz.friend.RefuseService;
import com.impte.wecard.biz.friend.RetreatRoomService;
import com.impte.wecard.utils.RespUtil;
import java.util.Map;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@AllArgsConstructor
public class RefuseController {

    private final RefuseService refuseService;

    private final RetreatRoomService retreatRoomService;

    /**
     * 拒绝请求，好友和房间请求通用(GET)
     * @param requestId 请求id
     * @return JSON
     */
    @GetMapping("/refuse/request")
    public Map<String, String> refuseFriendRequest(@RequestParam String requestId){
        return RespUtil.packMsg(refuseService.refuseRequest(requestId));
    }

    /**
     * 退出群(GET)
     * @param roomId 群id
     * @return JSON
     */
    @GetMapping("/quit/room")
    public Map<String, String> retreatRoom( @RequestParam String roomId){
        return RespUtil.packMsg(retreatRoomService.retreatRoom(roomId));
    }
}
