package com.impte.wecard.dao;

import com.impte.wecard.domain.po.CardImage;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author justZero
 * @since 2018/3/8
 */
@Repository
public interface CardImageDao extends GenericDao<CardImage, String> {

    List<CardImage> findByCardId(String cardId);

    int updateCardImgRemark(@Param("id") String id,
        @Param("newRemark") String newRemark);

    int insertList(@Param("cardImages") List<CardImage> cardImages);

    /**
     * 删除图片和卡片的关系
     * @param cardId 卡片ID
     * @return 删除的条数
     */
    int delCardImage(String cardId);
}
