package com.impte.wecard.controller;

import com.impte.wecard.biz.user.FindFollowService;
import com.impte.wecard.biz.user.FollowService;
import com.impte.wecard.biz.user.LookUserService;
import com.impte.wecard.utils.RespUtil;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@AllArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final LookUserService lookUserService;

    private final FollowService followService;

    private final FindFollowService findFollowService;

    /**
     * 查看某个用户基本信息(GET)
     * @param userId 用户id
     * @return JSON
     */
    @GetMapping("/getUser")
    public Map<String, Object> lookUser(@RequestParam String userId){
        return lookUserService.lookUser(userId);
    }

    /**
     * 关注某人(GET)
     * @param followingId 被关注者的id
     * @return JSON
     */
    @GetMapping("/follow")
    public Map<String, String> follow(@RequestParam String followingId){
        return RespUtil.packMsg(followService.follow(followingId));
    }

    /**
     * 取消关注(GET)
     * @param followingId 要取消关注的被关注者的id
     * @return JSON
     */
    @GetMapping("/unfollow")
    public Map<String, String> unFollow(@RequestParam String followingId){
        return RespUtil.packMsg(followService.unFollow(followingId));
    }

    /**
     * 查找自己关注的所有人(GET)
     * @return JSON
     */
    @GetMapping("/find/following")
    public Map<String, Object> findFollowing(){
        return findFollowService.findFollowing();
    }

    /**
     * 查找自己所有的粉丝(GET)
     * @return JSON
     */
    @GetMapping("/find/fans")
    public Map<String, Object> findFans(){
        return findFollowService.findFans();
    }
}
