package com.impte.wecard.controller;

import com.impte.wecard.biz.find.FindChatItemService;
import com.impte.wecard.biz.find.FindGroupService;
import com.impte.wecard.biz.find.FindMsgService;
import com.impte.wecard.biz.find.FindRequestService;
import com.impte.wecard.biz.find.FindRoomService;
import com.impte.wecard.biz.find.FindRoomsService;
import com.impte.wecard.biz.find.FindUsersService;
import java.util.Map;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@AllArgsConstructor
@RequestMapping("/find")
public class FindController {

    private final FindGroupService findGroupService;
    private final FindChatItemService findChatItemService;
    private final FindMsgService findMsgService;
    private final FindUsersService findUsersService;
    private final FindRoomsService findRoomsService;
    private final FindRequestService findRequestService;
    private final FindRoomService findRoomService;

    /**
     * 查找单个分组，包含里面的好友(GET)
     * @param groupId 分组的id
     * @return JSON
     */
    @GetMapping("/group")
    public Map<String, Object> findGroup(@RequestParam String groupId){
        return findGroupService.findGroup(groupId);
    }

    /**
     * 查找单个消息体(GET)
     * @param chatItemId 消息体id
     * @return JSON
     */
    @GetMapping("/chatItem")
    public Map<String, Object> findChatItem(@RequestParam String chatItemId){
        return findChatItemService.findChatItem(chatItemId);
    }

    /**
     * 查找单个消息(GET)
     * @param messageId 消息的id
     * @return JSON
     */
    @GetMapping("/message")
    public Map<String, Object> findMessage(@RequestParam String messageId){
        return findMsgService.findMessage(messageId);
    }

    /**
     * 查找单个群(GET)
     * @param roomId 群id
     * @return JSON
     */
    @GetMapping("/room")
    public Map<String, Object> findRoom(@RequestParam String roomId){
        return findRoomService.findRoom(roomId);
    }

    /**
     * 模糊搜索用户，默认返回8条(GET)
     * @param username 用户名，电话或昵称
     * @return JSON
     */
    @GetMapping("/users")
    public Map<String, Object> findUsers(@RequestParam String username){
        return findUsersService.findUsers(username);
    }

    /**
     *懒加载模糊搜索用户(GET)
     * @param username 用户名，电话或昵称
     * @param offset 起始数
     * @param limit 条数
     * @return JSON
     */
    @GetMapping("/users/limit")
    public Map<String, Object> findUsersLimit(@RequestParam String username, @RequestParam int offset,
            @RequestParam int limit){
        return findUsersService.findUsersLimit(username, offset, limit);
    }

    /**
     * 模糊搜索群(GET)
     * @param roomName 群名，群id
     * @return JSON
     */
    @GetMapping("/rooms")
    public Map<String, Object> findRooms(@RequestParam String roomName){
        return findRoomsService.findRooms(roomName);
    }

    /**
     * 懒加载模糊搜索群(GET)
     * @param roomName 群名，群id
     * @param offset 起始数量
     * @param limit 条数
     * @return JSON
     */
    @GetMapping("/rooms/limit")
    public Map<String, Object> findRoomsLimit(
            @RequestParam String roomName,
            @RequestParam int offset,
            @RequestParam int limit
            ){
        return findRoomsService.findRoomsLimit(roomName, offset, limit);
    }

    /**
     * 查找某个请求，包括好友请求和加群请求(GET)
     * @param requestId 好友请求或加群请求的id
     * @return JSON
     */
    @GetMapping("/request")
    public Map<String, Object> findRequest(@RequestParam String requestId){
        return findRequestService.findRequest(requestId);
    }

    /**
     * 精确查询单个用户基本信息
     * @param userId userId
     * @return JSON
     */
    @GetMapping("/simple/user")
    public Map<String, Object> findSimpleUser(@RequestParam String userId){
        return findUsersService.findSimpleUser(userId);
    }

    /**
     * 精确查询单个群基本信息
     * @param roomId roomId
     * @return JSON
     */
    @GetMapping("/simple/room")
    public Map<String, Object> findSimpleRoom(@RequestParam String roomId){
        return findRoomService.findSimpleRoom(roomId);
    }
}
