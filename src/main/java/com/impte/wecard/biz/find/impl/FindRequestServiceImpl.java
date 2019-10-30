package com.impte.wecard.biz.find.impl;

import com.impte.wecard.biz.find.FindRequestService;
import com.impte.wecard.dao.RequestDao;
import com.impte.wecard.domain.po.Request;
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
public class FindRequestServiceImpl implements FindRequestService {

    private final RequestDao requestDao;

    @Override
    public Map<String, Object> findRequest(String requestId) {
        String message;
        Map<String, Object> map = new HashMap<>();
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (requestId == null || requestId.equals("")){
            message = "RequestId cannot be empty";
        }else {
            //找到request
            Request request = requestDao.findRequest(requestId, user.getId());
            if (request == null){
                message = "Unknown request";
            }else {
                map.put("request", request);
                message = "Find success";
            }
        }
        map.put("message", message);

        return map;
    }
}
