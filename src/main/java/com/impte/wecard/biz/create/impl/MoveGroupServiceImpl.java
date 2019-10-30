package com.impte.wecard.biz.create.impl;

import com.impte.wecard.biz.create.MoveGroupService;
import com.impte.wecard.dao.FriendDao;
import com.impte.wecard.dao.GroupDao;
import com.impte.wecard.domain.po.Friend;
import com.impte.wecard.domain.po.Group;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MoveGroupServiceImpl implements MoveGroupService {

    private final GroupDao groupDao;

    private final FriendDao friendDao;

    @Override
    public String MoveGroup(String groupId, String friendId) {
        String message;

        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (groupId == null || groupId.equals("")){
            message = "GroupId cannot be empty";
        }else if (friendId == null || friendId.equals("")){
            message = "FriendId cannot be empty";
        }else {
            String userId = user.getId();
            //检测分组是否存在
            Group group = groupDao.verifyGroupExist(groupId, userId);
            //检测对方是否是自己的好友
            Friend friend = friendDao.findFriend(userId, friendId);
            if (group == null){
                message = "Group does not exist";
            }else if (friend == null){
                message = "Friend does not exist";
            }else if (friend.getGroup().getId() != null && friend.getGroup().getId().equals(groupId)){
                message = "Friend is already in this group";
            }else {
                //修改数据库中的friendItem中的groupId
                int result = friendDao.updateGroupId(groupId, userId, friendId);
                if (result == 0){
                    message = "Move fail";
                }else{
                    message = "Move success";
                }
            }
        }
        return message;
    }
}
