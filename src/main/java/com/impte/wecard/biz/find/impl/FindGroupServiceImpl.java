package com.impte.wecard.biz.find.impl;

import com.impte.wecard.biz.find.FindGroupService;
import com.impte.wecard.dao.GroupDao;
import com.impte.wecard.domain.po.Group;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FindGroupServiceImpl implements FindGroupService {

    private final GroupDao groupDao;

    @Override
    public Map<String, Object> findGroup(String groupId) {
        String message;
        Map<String, Object> map = new HashMap<>();
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (groupId == null || groupId.equals("")){
            message = "GroupId cannot be empty";
        }else {
            Group group = groupDao.findById(groupId);
            if (group == null) {
                message = "Group does not exist";
            }else if (!group.getUser().getId().equals(user.getId())){
                message = "Group permission error";
            }else {
                group.setUser(null);
                map.put("group", group);
                message = "Find success";
            }
        }
        map.put("message", message);

        return map;
    }
}
