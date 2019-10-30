package com.impte.wecard.biz.card;

import com.impte.wecard.domain.po.Comment;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * @author justZero
 * @since 2018-3-9
 */
public interface CommentService {

    /**
     * 加载一级评论<br/>
     * 包含评论的二级评论
     * @param cardId 卡片ID
     * @param start 评论起始
     * @param limit 评论条数
     * @param sortKey 排序关键字：poplar|time
     * @return map
     */
    Map<String, Object> getCmntsByCardId(
        String cardId, Integer start, Integer limit, String sortKey
    );

    /**
     * 加载一级评论
     * @param cardId 卡片ID
     * @param start 评论起始
     * @param limit 评论条数
     * @param sortKey 排序关键字：poplar|time
     * @return map
     */
    Map<String, Object> getLv1Cmnts(
        String cardId, Integer start, Integer limit, String sortKey
    );

    /**
     * 加载二级评论
     * @param commentId 父评论ID
     * @param start 评论起始
     * @param limit 评论条数
     * @return map
     */
    Map<String, Object> getLv2Cmnts(
        String commentId, Integer start, Integer limit
    );

    Map<String, Object> userLikeCmnt(String commentId);

    Map<String, Object> userUnlikeCmnt(String commentId
    );

    Map<String, Object> createCmnt(Comment comment);

    Map<String, Object> delCmnt(String commentId);

    Map<String, Object> getUserCmnt(int start, int limit);

    Map<String, Object> getCmntToUser(int start, int limit);

}
