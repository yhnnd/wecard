package com.impte.wecard.biz.card;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author justZero
 * @since 2018-3-6
 */
public interface CardService {

    /**
     * 获取热门卡片预览数据
     * @param start 起始
     * @param limit 卡片数
     * @return Map
     */
    Map<String, Object> getCardPreviews(int start, int limit, String sortKey);

    /**
     * 获取用户卡片预览数据
     * @param start 起始
     * @param limit 卡片数
     * @param sortKey 排序关键字：poplar|time
     * @return map
     */
    Map<String, Object> getUserCardPreviews(int start, int limit, String sortKey);

    /**
     * 获取用户喜欢的卡片
     * 需要用户登录<br/>
     * @param start 起始
     * @param limit 卡片数
     * @return Map
     */
    Map<String, Object> getLikedCardPreviews( int start, int limit);

    /**
     * 获取用户转发的卡片
     * 需要用户登录<br/>
     * @param start 起始
     * @param limit 卡片数
     * @return Map
     */
    Map<String, Object> getSharedCardPreviews(int start, int limit);

    /**
     * 获取卡片全部数据
     * @param cardId 卡片ID
     * @param start 评论起始
     * @param limit 评论条数
     * @param sortKey 评论排序方式：popular|time
     * @return Map
     */
    Map<String, Object> getCardById(
        String cardId, Integer start, Integer limit, String sortKey);

    /**
     * 获取分享卡片全部数据
     * @param cardId 卡片ID
     * @param start 评论起始
     * @param limit 评论条数
     * @param sortKey 评论排序方式：popular|time
     * @return Map
     */
    Map<String, Object> getShareCardById(String cardId, Integer start, Integer limit, String sortKey);

    Map<String, Object> getFollowingCardByUserId(int start, int limit);

    Map<String, Object> userLikeCard(String cardId);

    Map<String,Object> userUnlikeCard(String cardId);

    Map<String, Object> createCard(String title, String text, String type, String[] topics);

    Map<String, Object> createShareCard(String text, String type, String shareId);

    Map<String, Object> createImageCard(String title, String text, String type, String[] topics,
        String[] remarks, MultipartFile[] files);

    Map<String, Object> createVideoCard(String title, String text, String type, String[] topics,
        String fileName, MultipartFile file);

    Map<String, Object> createVoiceCard(String title, String text, String type, String[] topics,
        String fileName, MultipartFile file);

    /**
     * 卡片标记删除
     * <p>不删除卡片下的评论
     * @param cardId 卡片ID
     * @return map
     */
    Map<String, Object> delCard(String cardId);

    /**
     * 获取用户卡片总数
     * @param userId 用户ID
     * @return 卡片总数
     */
    Map<String, Object> getUserCardsCount(String userId);

    Map<String, Object> getUserLikedCardsCount(String userId);

    Map<String ,Object> getUserSharedCardsCount(String userId);

}
