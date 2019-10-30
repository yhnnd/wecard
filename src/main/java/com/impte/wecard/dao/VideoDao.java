package com.impte.wecard.dao;

import com.impte.wecard.domain.po.Video;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author justZero
 * @since 2018-3-8
 */
@Repository
public interface VideoDao extends GenericDao<Video, String> {

    Video findByCardId(String cardId);

    int updateVideoName(@Param("id") String id,
        @Param("newName") String newName);

}
