package com.impte.wecard.biz.remove.impl;

import com.impte.wecard.biz.remove.RemoveGroupService;
import com.impte.wecard.dao.GroupDao;
import com.impte.wecard.domain.po.Group;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RemoveGroupServiceImpl implements RemoveGroupService {

    private final GroupDao groupDao;

    @Override
    public String RemoveGroup(String groupId) {
        String message;
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (groupId == null || groupId.equals("")){
            message = "GroupId cannot be empty";
        }else {
            //验证group是否合法
            Group group = groupDao.verifyGroupExist(groupId, user.getId());
            if (group == null){
                message = "Group does not exist";
            }else {
                //验证group里是否存在好友
                int friendNum = groupDao.findGroupFriendNum(groupId);
                if (friendNum != 0){
                    message = "Group is not empty";
                }else {
                    int result = groupDao.delete(groupId);
                    if (result == 0){
                        message = "Remove fail";
                    }else {
                        message = "Remove success";
                    }
                }
            }
        }

        return message;
    }
}
