package com.impte.wecard.biz.card;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * 话题相关服务：
 * <p/>
 *  1.话题创建后不允许修改；<br/>
 *  2.话题被卡片引用后不允许删除；<br/>
 * @author justZero
 * @since 2018-3-9
 */
public interface TopicService {

    Map<String, Object> createTopic(HttpServletRequest request, String topicName);

    Map<String, Object> deleteTopic(HttpServletRequest request, String topicId);

    /**
     * 根据话题名模糊查找话题数据
     * @return map
     */
    Map<String, Object> getTopicsByName(String topicName);
    
    Map<String, Object> getTopicsByCreatorId(String creatorId);
}
