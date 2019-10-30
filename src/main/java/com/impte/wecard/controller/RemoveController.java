package com.impte.wecard.controller;

import com.impte.wecard.biz.remove.DisableAdminService;
import com.impte.wecard.biz.remove.RecallMessageService;
import com.impte.wecard.biz.remove.RemoveChatItemService;
import com.impte.wecard.biz.remove.RemoveFriendService;
import com.impte.wecard.biz.remove.RemoveGroupService;
import com.impte.wecard.biz.remove.RemoveMessageService;
import com.impte.wecard.biz.remove.RemoveRMService;
import com.impte.wecard.biz.remove.RemoveRoomService;
import com.impte.wecard.utils.RespUtil;
import java.util.Map;

import lombok.AllArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@AllArgsConstructor
@RequestMapping("/remove")
public class RemoveController {

    private final RemoveChatItemService removeChatItemService;
    private final RemoveGroupService removeGroupService;
    private final RemoveRMService removeRMService;
    private final RemoveFriendService removeFriendService;
    private final DisableAdminService disableAdminService;
    private final RemoveMessageService removeMessageService;
    private final RecallMessageService recallMessageService;
    private final RemoveRoomService removeRoomService;

    /**
     * 移除消息体(GET)
     * @param chatItemId 消息体id
     * @return JSON
     */
    @GetMapping("/chatItem")
    public Map<String, String> removeChatItem(@RequestParam String chatItemId){
        return RespUtil.packMsg(removeChatItemService.removeChatItem(chatItemId));
    }

    /**
     * 删除分组(GET)
     * @param groupId 分组id
     * @return JSON
     */
    @GetMapping("/group")
    public Map<String, String> removeGroup(@RequestParam String groupId){
        return RespUtil.packMsg(removeGroupService.RemoveGroup(groupId));
    }

    /**
     * 提出群成员(GET)
     * @param roomId 群id
     * @param memberId 群成员id
     * @return JSON
     */
    @GetMapping("/roomMember")
    public Map<String, String> removeRoomMember(@RequestParam String roomId, @RequestParam String memberId){
        return RespUtil.packMsg(removeRMService.removeMemberService(roomId, memberId));
    }

    /**
     * 删除好友(GET)
     * @param friendId 好友id
     * @return JSON
     */
    @GetMapping("/friend")
    public Map<String, String> removeFriend(@RequestParam String friendId){
        return RespUtil.packMsg(removeFriendService.removeFriend(friendId));
    }

    /**
     * 取消管理员(GET)
     * @param adminId 管理员id
     * @param roomId 群id
     * @return JSON
     */
    @GetMapping("/admin")
    public Map<String, String> removeAdmin(@Param("adminId") String adminId, @Param("roomId") String roomId){
        return RespUtil.packMsg(disableAdminService.disableAdmin(adminId, roomId));
    }

    /**
     * 删除好友消息(GET)
     * @param messageId 消息id
     * @return JSON
     */
    @GetMapping("/friend/message")
    public Map<String, String> removeFriendMessage(@RequestParam String messageId){
        return RespUtil.packMsg(removeMessageService.removeFriendMessage(messageId));
    }

    /**
     *删除群消息(GET)
     * @param messageId 消息id
     * @return JSON
     */
    @GetMapping("/room/message")
    public Map<String, String> removeRoomMessage(@RequestParam String messageId){
        return RespUtil.packMsg(removeMessageService.removeRoomMessage(messageId));
    }

    /**
     * 撤回好友消息(GET)
     * @param messageId 消息id
     * @param friendId 朋友id
     * @return JSON
     */
    @GetMapping("/friend/recall")
    public Map<String, String> recallFriendMessage(@RequestParam String messageId, @RequestParam String friendId){
        return RespUtil.packMsg(recallMessageService.recallFriendMessage(messageId, friendId));
    }

    /**
     *撤回群消息(GET)
     * @param messageId 消息id
     * @param roomId 群id
     * @return JSON
     */
    @GetMapping("/room/recall")
    public Map<String, String> recallRoomMessage(@RequestParam String messageId, @RequestParam String roomId){
        return RespUtil.packMsg(recallMessageService.recallRoomMessage(messageId, roomId));
    }

    /**
     * 解散群(GET)
     * @param roomId 群id
     * @return JSON
     */
    @GetMapping("/room")
    public Map<String, String> removeRoom(@Param("roomId") String roomId){
        return RespUtil.packMsg(removeRoomService.removeRoom(roomId));
    }
}
