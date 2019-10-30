package com.impte.wecard.controller;

import com.impte.wecard.biz.card.TopicService;
import java.util.Map;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author justZero
 * @since 2018/3/9
 */
@RestController
@CrossOrigin
@AllArgsConstructor
@RequestMapping("/topic")
public class TopicController {

    private final TopicService topicService;

    /**
     * 根据话题名，模糊查找话题(GET)
     * @param topicName 话题名
     * @return JSON
     */
    @GetMapping("/search")
    public Map<String, Object> searchTopicByName(String topicName) {
        return topicService.getTopicsByName(topicName);
    }
    
    /**
     * 根据创建者，查找该用户创建的所有话题(GET)
     * @param creatorId
     * @return JSON
     */
    @GetMapping("/search/creator")
    public Map<String, Object> getTopicsByCreatorId(String creatorId) {
        return topicService.getTopicsByCreatorId(creatorId);
    }

}
