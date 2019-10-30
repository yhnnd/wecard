package com.impte.wecard.biz.user.impl;

import com.impte.wecard.biz.user.FollowService;
import com.impte.wecard.dao.FollowDao;
import com.impte.wecard.dao.UserDao;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import com.impte.wecard.utils.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final UserDao userDao;

    private final FollowDao followDao;

    @Override
    public String follow(String followingId) {
        String message;
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (followingId == null || followingId.equals("")){
            message = "FollowingId cannot be empty";
        }else if (followingId.equals(user.getId())){
            message = "Cannot follow yourself";
        }else{
            String userId = user.getId();
            //验证following 是否存在
            User following = userDao.verifyUserExistById(followingId);
            //验证是否已经关注
            String verifyResult = followDao.verifyHasFollowed(userId, followingId);
            if (following == null){
                message = "Following does not exist";
            }else if (verifyResult != null){
                message = "Has followed";
            }else {
                int result = followDao.follow(UUID.getUUID(), userId, followingId);
                if (result == 0){
                    message = "Follow fail";
                }else {
                    message = "Follow success";
                }
            }
        }
        return message;
    }

    @Override
    public String unFollow(String followingId) {
        String message;
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (followingId == null || followingId.equals("")){
            message = "FollowingId cannot be empty";
        }else if (followingId.equals(user.getId())){
            message = "Cannot unfollow yourself";
        }else{
            String userId = user.getId();
            //验证following 是否存在
            String followId = followDao.verifyHasFollowed(userId, followingId);
            if (followId == null){
                message = "You are not following him";
            }else {
                int result = followDao.unFollow(followId);
                if (result == 0){
                    message = "Unfollow fail";
                }else {
                    message = "Unfollow success";
                }
            }
        }
        return message;
    }
}
