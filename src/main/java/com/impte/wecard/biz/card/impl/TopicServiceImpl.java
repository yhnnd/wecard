package com.impte.wecard.biz.card.impl;

import com.impte.wecard.biz.card.TopicService;
import com.impte.wecard.dao.TopicDao;
import com.impte.wecard.domain.po.Topic;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CheckUtil;
import com.impte.wecard.utils.UUID;
import com.impte.wecard.utils.web.ResponseMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * @author justZero
 * @since 2018/3/9
 */
@Service
@AllArgsConstructor
public class TopicServiceImpl implements TopicService {

    private final TopicDao topicDao;

    private final DataSourceTransactionManager txManager;

    @Override
    public Map<String, Object> createTopic(HttpServletRequest request, String topicName) {
        Map<String, Object> response = new HashMap<>(2);
        HttpSession session = request.getSession();
        // 用户必须在线
        User user = (User) session.getAttribute("user");

        // 已经过登录状态检查拦截器鉴权

        // 检测话题名是否合法
        if (topicName.length() > Topic.NAME_MAX_LEN) {
            response.put("message", ResponseMessage.TOPIC_NAME_TOO_LONG);
            return response;
        }
        // 只允许中文话题名
        if (CheckUtil.isCHTxtLegal(topicName)) {
            response.put("message", ResponseMessage.TOPIC_NAME_ILLEGAL);
            return response;
        }

        Topic topic = new Topic(UUID.getUUID(), topicName, user);

        DefaultTransactionDefinition dtd = new DefaultTransactionDefinition();
        dtd.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus txStatus = txManager.getTransaction(dtd);
        try {
            int res = topicDao.insert(topic);
            if (res != 1) {
               throw new RuntimeException(ResponseMessage.TOPIC_CREATE_FAILED);
            }
            txManager.commit(txStatus);
        } catch (Exception e) {
            e.printStackTrace();
            txManager.rollback(txStatus);
            response.put("message", ResponseMessage.TOPIC_CREATE_FAILED);
            return response;
        }

        response.put("topicId", topic.getId());
        response.put("message", ResponseMessage.TOPIC_CREATE_SUCCESS);
        return response;
    }

    @Override
    public Map<String, Object> deleteTopic(HttpServletRequest request, String topicId) {
        Map<String, Object> response = new HashMap<>(1);
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        // 已经过登录状态检查拦截器鉴权

        // 用户不是话题创建者，无法删除话题
        if (isTopicCreator(topicId, user.getId())) {
            response.put("message", ResponseMessage.NOT_TOPIC_CREATOR);
            return response;
        }

        DefaultTransactionDefinition dtd = new DefaultTransactionDefinition();
        dtd.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus txStatus = txManager.getTransaction(dtd);
        try {
            int res = topicDao.delete(topicId);
            if (res != 1) {
                response.put("message", ResponseMessage.TOPIC_DELETE_FAILED);
                return response;
            }
            txManager.commit(txStatus);
        } catch (Exception e) {
            e.printStackTrace();
            txManager.rollback(txStatus);
            response.put("message", ResponseMessage.TOPIC_IS_USING);
            return response;
        }

        response.put("message", ResponseMessage.TOPIC_DELETE_SUCCESS);
        return response;
    }

    @Override
    public Map<String, Object> getTopicsByName(String topicName) {
        Map<String, Object> response = new HashMap<>(2);

        try {
            List<Topic> topics = topicDao.findByName(topicName);
            if (topics.size() <= 0) {
                response.put("message", ResponseMessage.NO_DATA);
                return response;
            }
            response.put("topics", topics);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", ResponseMessage.TOPIC_LOAD_FAILED);
            return response;
        }

        response.put("message", ResponseMessage.TOPIC_LOAD_SUCCESS);
        return response;
    }

    /**
     * 判断是不是话题创建者
     */
    private boolean isTopicCreator(String topicId, String creatorId) {
        return topicDao.findByIdAndCreatorId(topicId, creatorId) != null;
    }
    
    /**
     * 获取用户创建的所有话题
     */
    @Override
    public Map<String, Object> getTopicsByCreatorId(String creatorId) {
        Map<String, Object> response = new HashMap<>(2);

        try {
            List<Topic> topics = topicDao.findByCreatorId(creatorId);
            if (topics.size() <= 0) {
                response.put("message", ResponseMessage.NO_DATA);
                return response;
            }
            response.put("topics", topics);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", ResponseMessage.TOPIC_LOAD_FAILED);
            return response;
        }

        response.put("message", ResponseMessage.TOPIC_LOAD_SUCCESS);
        return response;
    }
}
