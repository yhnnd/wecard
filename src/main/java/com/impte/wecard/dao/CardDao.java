package com.impte.wecard.dao;

import com.impte.wecard.domain.po.Card;
import com.impte.wecard.domain.po.CardImage;
import com.impte.wecard.domain.po.Comment;
import com.impte.wecard.domain.po.Topic;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author justZero
 * @since 2018/3/6
 */
@Repository
public interface CardDao extends GenericDao<Card, String> {

    Card findOne(
        @Param("id") String id,
        @Param("currentUserId") String currentUserId
    );

    /**
     * 查询卡片预览
     * @param offset
     * @param limit
     * @param sortKey
     * @return
     */
    List<Card> findPreviews(
        @Param("offset") int offset,
        @Param("limit") int limit,
        @Param("sortKey") String sortKey,
        @Param("currentUserId") String currentUserId
    );

    /**
     * 查询用户卡片
     * @param userId 用户ID
     * @param sortKey 排序关键字：popular|time
     * @return
     */
    List<Card> findByUserId(
        @Param("userId") String userId,
        @Param("offset") int offset,
        @Param("limit") int limit,
        @Param("sortKey") String sortKey,
        @Param("currentUserId") String currentUserId
    );

    List<Card> findLikedByUserId(
        @Param("userId") String userId,
        @Param("offset") int offset,
        @Param("limit") int limit,
        @Param("currentUserId") String currentUserId
    );

    List<Card> findSharedByUserId(
        @Param("userId") String userId,
        @Param("offset") int offset,
        @Param("limit") int limit,
        @Param("currentUserId") String currentUserId
    );

    Card findShareById(@Param("id") String id, @Param("currentUserId") String currentUserId);

    List<Card> findFollowingByUserId(
        @Param("userId") String userId,
        @Param("offset") int offset,
        @Param("limit") int limit,
        @Param("currentUserId") String currentUserId
    );

    int insertLikeCard(@Param("id") String id,
        @Param("cardId") String cardId,
        @Param("userId") String userId);

    int deleteLikeCard(@Param("cardId") String cardId,
        @Param("userId") String userId);

    int insertCardTopic(@Param("id") String id,
        @Param("cardId") String cardId,
        @Param("topicId") String topicId);

    /**
     * 删除卡片和话题的关联
     * @param cardId 卡片ID
     * @return 删除的条数
     */
    int delCardTopic(String cardId);

    /**
     * 查询卡片话题个数
     * @param cardId 卡片ID
     * @return
     */
    int countTopicByCardId(String cardId);

    /**
     * 根据内容模糊查找卡片
     * 内容可能是：标题、内容、用户名
     * @param content 待查找内容
     * @return
     */
    List<Card> findByContent(
        @Param("content") String content,
        @Param("offset") int offset,
        @Param("limit") int limit,
        @Param("sortKey") String sortKey,
        @Param("currentUserId") String currentUserId
    );

    /**
     * 根据话题名精准查找某话题下的卡片
     * @param topicName
     * @param offset
     * @param limit
     * @param sortKey
     * @return
     */
    List<Card> findByTopicName(
        @Param("topicName") String topicName,
        @Param("offset") int offset,
        @Param("limit") int limit,
        @Param("sortKey") String sortKey,
        @Param("currentUserId") String currentUserId
    );

    Card findByIdAndUserId(@Param("id") String id,
        @Param("userId") String userId);

    Card findByIdAndType(@Param("id") String id,
        @Param("type") String type);

    /**
     * 计算用户卡片总数
     * @return
     */
    int countUserCards(String userId);

    int countUserLikedCards(String userId);

    int countUserSharedCards(String userId);

    int updateShareNumAddOne(String shareId);

    int countLikeByIdAndUserId(@Param("cardId") String cardId,
        @Param("userId") String userId);

    String findStatusById(String cardId);

    // DAO层私用方法
    List<CardImage> getImagesByCardId(String cardId);
    List<Topic> getTopicsByCardId(String cardId);
    List<Comment> getLv1CmntByCardId(String cardId);

}
