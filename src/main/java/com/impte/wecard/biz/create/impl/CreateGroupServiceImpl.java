package com.impte.wecard.biz.create.impl;

import com.impte.wecard.biz.chat.OnlineMsgService;
import com.impte.wecard.biz.create.CreateGroupService;
import com.impte.wecard.dao.GroupDao;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import com.impte.wecard.utils.UUID;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CreateGroupServiceImpl implements CreateGroupService {

    private final GroupDao groupDao;

    private final OnlineMsgService onlineMsgService;

    @Override
    public String createGroup(String groupName) {
        String message;
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (groupName == null || "".equals(groupName)){
            message = "GroupName cannot be empty";
        }else if (groupName.length() > 24){
            message = "GroupName is too long";
        }else {
            //验证该用户是否已经存在这个分组名
            String grId = groupDao.verifyGroupNameExist(user.getId(), groupName);
            if (grId != null){
                message = "GroupName already exists";
            }else {
                //插入分组
                String groupId = UUID.getUUID();
                int result = groupDao.insertGroup(groupId, groupName, user.getId());
                if (result != 1){
                    message = "Create fail";
                }else {
                    message = "Create success";
                    onlineMsgService.sendCommand(user.getId(), "groupId", groupId, "load_new_group");
                }
            }
        }

        return message;
    }
}
