package com.impte.wecard.dao;

import com.impte.wecard.domain.po.Voice;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author justZero
 * @since 2018-3-8
 */
@Repository
public interface VoiceDao extends GenericDao<Voice, String> {

    Voice findByCardId(String cardId);

    int updateVoiceName(@Param("id") String id,
        @Param("newName") String newName);

}
