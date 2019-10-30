package com.impte.wecard.dao;

import com.impte.wecard.domain.po.Request;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestDao extends GenericDao<Request, String> {

    //用于加载一个人所有的请求和收到的请求
    List<Request> findUserRequests(
        @Param("userId") String userId,
        @Param("offset") int offset,
        @Param("limit") int limit
    );

    int insertFriendRequest(
        @Param("id") String id,
        @Param("requestGroupId") String groupId,
        @Param("requestRemark") String remark,
        @Param("requesterId") String requesterId,
        @Param("receiverId") String receiverId,
        @Param("requestMessage") String requestMessage
    );

    int insertRoomRequest(
        @Param("id") String id,
        @Param("requesterId") String requesterId,
        @Param("roomId") String roomId,
        @Param("requestMessage") String requestMessage
    );

    int insertReceiver(
        @Param("id") String id,
        @Param("receiverId") String receiverId,
        @Param("requestId") String requestId
    );

    int setStatusAndReplierId(
        @Param("id") String requestId,
        @Param("status") String status,
        @Param("replierId") String replierId
    );

    //验证用户是否是接受者
    String verifyHandler(
        @Param("requestId") String requestId,
        @Param("handlerId") String handlerId
    );

    Request findRequest(
        @Param("id") String id,
        @Param("userId") String userId
    );
}
