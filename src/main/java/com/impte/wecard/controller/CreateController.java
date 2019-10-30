package com.impte.wecard.controller;

import com.impte.wecard.biz.create.CreateGroupService;
import com.impte.wecard.biz.create.CreateRoomService;
import com.impte.wecard.biz.create.MoveGroupService;
import com.impte.wecard.biz.create.SetNameService;
import com.impte.wecard.utils.RespUtil;
import java.util.Map;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@AllArgsConstructor
public class CreateController {

    private final CreateGroupService createGroupService;
    private final CreateRoomService createRoomService;
    private final SetNameService setNameService;
    private final MoveGroupService moveGroupService;

    /**
     * 创建好友分组(GET)
     * @param groupName 分组名
     * @return JSON
     */
    @GetMapping("/create/group")
    public Map<String, String> createGroup(@RequestParam String groupName){
        return RespUtil.packMsg(createGroupService.createGroup(groupName));
    }

    /**
     * 创建群(GET)
     * @param roomName 群名称
     * @return JSON
     */
    @GetMapping("/create/room")
    public Map<String, String> createRoom(@RequestParam String roomName){
        return RespUtil.packMsg(createRoomService.createRoom(roomName));
    }

    /**
     * 设置分组名(GET)
     * @param groupName 分组名
     * @param groupId 分组的id
     * @return JSON
     */
    @GetMapping("/setGroupName")
    public Map<String, String> setGroupName(
            @RequestParam String groupName,
            @RequestParam String groupId
            ){
        return RespUtil.packMsg(setNameService.setGroupName(groupId, groupName));
    }

    /**
     * 移动一个分组里的好友到另外一个分组(GET)
     * @param friendId 被移动的好友的id
     * @param groupId 新分组的id
     * @return JSON
     */
    @GetMapping("/moveFriendToGroup")
    public Map<String, String> moveFriendToGroup(
            @RequestParam String friendId,
            @RequestParam String groupId
            ){
        return RespUtil.packMsg(moveGroupService.MoveGroup(groupId, friendId));
    }

}
