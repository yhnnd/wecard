package com.impte.wecard.dao;

import com.impte.wecard.domain.po.Topic;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author justZero
 * @since 2018-3-9
 */
@Repository
public interface TopicDao extends GenericDao<Topic, String> {

    List<Topic> findByCreatorId(String creatorId);

    Topic findByIdAndCreatorId(@Param("id") String id, @Param("creatorId") String creatorId);

    Topic findByFullName(String name);
}
