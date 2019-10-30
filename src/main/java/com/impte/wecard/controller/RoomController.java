package com.impte.wecard.controller;

import com.impte.wecard.biz.create.SetNameService;
import com.impte.wecard.biz.create.SetRemarkService;
import com.impte.wecard.biz.create.SetRoomAdminService;
import com.impte.wecard.biz.find.FindRoomMembersService;
import com.impte.wecard.biz.friend.AgreeRoomService;
import com.impte.wecard.biz.friend.RequestService;
import com.impte.wecard.utils.RespUtil;
import java.util.Map;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@AllArgsConstructor
@RequestMapping("/room")
public class RoomController {

    private final RequestService requestService;
    private final AgreeRoomService agreeRoomService;
    private final SetRoomAdminService setRoomAdminService;
    private final SetRemarkService setRemarkService;
    private final FindRoomMembersService findRoomMembersService;
    private final SetNameService setNameService;

    /**
     * 发送申请加入群的请求(GET)
     * @param roomId 群id
     * @param requestMessage 验证消息
     * @return JSON
     */
    @GetMapping("/joinRoomRequest")
    public Map<String, String> joinRoomRequest(@RequestParam String roomId,
            @RequestParam(required = false, defaultValue = "") String requestMessage){
        return RespUtil.packMsg(requestService.joinRoomRequest(roomId, requestMessage));
    }

    /**
     * 同意加入群(GET)
     * @param requestId 请求id
     * @return JSON
     */
    @GetMapping("/agreeJoinRoomRequest")
    public Map<String, String> agreeJoinRoomRequest(@RequestParam String requestId){
        return RespUtil.packMsg(agreeRoomService.agreeRoomRequest(requestId));
    }

    /**
     * 查看房间成员(GET)
     * @param roomId 群id
     * @return JSON
     */
    @GetMapping("/members")
    public Map<String, Object> roomMembers(@RequestParam String roomId){
        return findRoomMembersService.findRoomMembers(roomId);
    }

    /**
     * 设置群管理员(GET)
     * @param roomId 群id
     * @param memberId 将要设为管理员的群成员id
     * @return JSON
     */
    @GetMapping("/set/admin")
    public Map<String, String> setRoomAdmin(
            @RequestParam("roomId") String roomId,
            @RequestParam("memberId") String memberId
            ){
        return RespUtil.packMsg(setRoomAdminService.SetRoomAdmin(memberId, roomId));
    }

    /**
     * 设置自己的群备注(GET)
     * @param roomId 群id
     * @param remark 群备注
     * @return JSON
     */
    @GetMapping("/set/remark")
    public Map<String, String> setRoomRemark(@RequestParam String roomId, @RequestParam String remark){
        return RespUtil.packMsg(setRemarkService.setRoomRemark(remark, roomId));
    }

    /**
     * 修改房间名称(GET)
     * @param roomId 群id
     * @param roomName 新的群名称
     * @return JSON
     */
    @GetMapping("/set/roomName")
    public Map<String, String> setRoomName(@RequestParam String roomId, @RequestParam String roomName){
        return RespUtil.packMsg(setNameService.setRoomName(roomId, roomName));
    }
}
