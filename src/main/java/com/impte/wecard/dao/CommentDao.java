package com.impte.wecard.dao;

import com.impte.wecard.domain.po.Comment;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author justZero
 * @since 2018-2-10
 */
@Repository
public interface CommentDao extends GenericDao<Comment, String> {

    List<Comment> findByCardId(
        @Param("cardId") String cardId,
        @Param("offet") Integer Offset,
        @Param("limit") Integer limit,
        @Param("sortKey") String sortKey);

    /**
     * 查询一级评论：卡片的评论
     * 根据sortKey实现排序方式
     * 默认时间排序
     * @param cardId
     * @param offset
     * @param limit
     * @param sortKey popular|time
     * @return
     */
    List<Comment> findLv1ByCardId(
        @Param("cardId") String cardId,
        @Param("offset") Integer offset,
        @Param("limit") Integer limit,
        @Param("sortKey") String sortKey);

    /**
     * 查询二级评论：评论的评论
     * 默认时间排序
     * @param commentId 父评论ID
     * @param offset
     * @param limit
     * @return
     */
    List<Comment> findLv2(
        @Param("commentId") String commentId,
        @Param("offset") Integer offset,
        @Param("limit") Integer limit);

    Comment findByIdAndUserId(@Param("id") String id,
        @Param("userId") String userId);

    String findStatusById(String commentId);

    List<Comment> findByUserId(@Param("userId") String userId,
        @Param("offset") int offset,
        @Param("limit") int limit);

    /**
     * 查询回复用户的评论
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    List<Comment> findToUserByUserId(@Param("userId") String userId,
        @Param("offset") int offset,
        @Param("limit") int limit);

    int insertLikeComment(
        @Param("id") String id,
        @Param("commentId") String commentId,
        @Param("userId") String userId);

    int deleteLikeComment(
        @Param("commentId") String commentId,
        @Param("userId") String userId);

    /**
     * 删除一级评论下：全部的二级评论
     * @param commentId 父评论ID
     * @return
     */
    int deleteAllLv2(@Param("commentId") String commentId);

    /**
     * 根据卡片ID删除评论
     * @param cardId 卡片ID
     * @return
     */
    int deleteByCardId(@Param("cardId") String cardId);

    int countLikeByIdAndUserId(@Param("commentId") String commentId,
        @Param("userId") String userId);

    // DAO层私用方法
    List<Comment> getChildByParentId(String parentId);

}
