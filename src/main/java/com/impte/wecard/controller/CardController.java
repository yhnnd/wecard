package com.impte.wecard.controller;

import com.impte.wecard.biz.card.CardSearchService;
import com.impte.wecard.biz.card.CardService;
import com.impte.wecard.domain.po.Card;
import java.util.Map;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author justZero
 * @since 2018/3/6
 */
@RestController
@CrossOrigin
@AllArgsConstructor
@RequestMapping("/card")
public class CardController {

    private final CardService cardService;
    private final CardSearchService cardSearchService;

    /**
     * 加载卡片（GET）
     * <p>热门或最新
     * @param start 卡片起始，默认0
     * @param limit 卡片个数，默认15
     * @param sortKey popular|time(default)
     * @return JSON
     */
    @GetMapping("/load")
    public Map<String, Object> loadCards(
            @RequestParam(required = false, defaultValue = "0") int start,
            @RequestParam(required = false, defaultValue = "15") int limit,
            @RequestParam(required = false, defaultValue = "time") String sortKey) {
        return cardService.getCardPreviews(start, limit, sortKey);
    }

    /**
     * 加载用户卡片（GET）
     * @param start 卡片起始
     * @param limit 卡片个数
     * @param sortKey popular|time(default)
     * @return JSON
     */
    @GetMapping("/mine")
    public Map<String, Object> loadUserCards(
            @RequestParam(required = false, defaultValue = "0") int start,
            @RequestParam(required = false, defaultValue = "15") int limit,
            @RequestParam(required = false, defaultValue = "time") String sortKey) {
        return cardService.getUserCardPreviews(start, limit, sortKey);
    }

    /**
     * 加载用户喜欢的卡片(GET)
     * <p>不过滤已被删除的卡片
     * @param start 卡片起始，默认0
     * @param limit 卡片个数，默认15
     * @return JSON
     */
    @GetMapping("/liked")
    public Map<String, Object> loadLikedCards(
            @RequestParam(required = false, defaultValue = "0") int start,
            @RequestParam(required = false, defaultValue = "15") int limit ) {
        return cardService.getLikedCardPreviews(start, limit);
    }

    /**
     * 加载用户分享的卡片(GET)
     * @param start 卡片起始，默认0
     * @param limit 卡片个数，默认15
     * @return JSON
     */
    @GetMapping("/shared")
    public Map<String, Object> loadSharedCards(
            @RequestParam(required = false, defaultValue = "0") int start,
            @RequestParam(required = false, defaultValue = "15") int limit) {
        return cardService.getSharedCardPreviews(start, limit);
    }

    /**
     * 进入分享卡片详情（GET）
     * @param cardId 卡片ID
     * @param start 评论起始，默认0
     * @param limit 评论条数，默认5条
     * @param sortKey 评论排序方式：popular|time(default)
     * @return JSON
     */
    @GetMapping("/share/read")
    public Map<String, Object> loadSharedCard(
            @RequestParam String cardId,
            @RequestParam(required = false, defaultValue = "0") int start,
            @RequestParam(required = false, defaultValue = "5") int limit,
            @RequestParam(required = false, defaultValue = "time") String sortKey) {
        return cardService.getShareCardById(cardId, start, limit, sortKey);
    }

    /**
     * 进入卡片详情(GET)
     * <p>默认加载5条一级评论（按时间顺序）
     * @param cardId 卡片ID
     * @param start 评论起始，默认0
     * @param limit 评论条数，默认5条
     * @param sortKey 评论排序方式：popular|time(default)
     * @return JSON
     */
    @GetMapping("/read")
    public Map<String, Object> loadCard(
            @RequestParam String cardId,
            @RequestParam(required = false, defaultValue = "0") int start,
            @RequestParam(required = false, defaultValue = "5") int limit,
            @RequestParam(required = false, defaultValue = "time") String sortKey) {
        return cardService.getCardById(cardId, start, limit, sortKey);
    }

    /**
     * 加载用户关注者的所有卡片
     * @param start 卡片起始，默认0
     * @param limit 卡片条数，默认5
     * @return JSON
     */
    @GetMapping("/following")
    public Map<String, Object> loadFollowingCards(
            @RequestParam(required = false, defaultValue = "0") int start,
            @RequestParam(required = false, defaultValue = "5") int limit) {
        return cardService.getFollowingCardByUserId(start, limit);
    }

    /**
     * 喜欢卡片(POST)
     * @param cardId 卡片ID
     * @return JSON
     */
    @PostMapping("/like")
    public Map<String, Object> likeCard(@RequestParam String cardId) {
        return cardService.userLikeCard(cardId);
    }

    /**
     * 取消喜欢卡片(POST)
     * @param cardId 卡片ID
     * @return JSON
     */
    @PostMapping("/unlike")
    public Map<String, Object> unlikeCard(@RequestParam String cardId) {
        return cardService.userUnlikeCard(cardId);
    }

    /**
     * 卡片搜索(GET)
     * <p>
     * 1.话题名搜索：查找某话题下全部卡片;<br>
     * 2.内容搜索：模糊查找卡片标题、内容、用户名包含关键字的卡片;<br>
     * @param key 搜索方式：topic|content
     * @param value 待搜索关键字
     * @param start 搜索结果起始，默认0
     * @param limit 搜索条数，默认15
     * @return JSON
     */
    @GetMapping("/search")
    public Map<String, Object> search(
            @RequestParam String key,
            @RequestParam String value,
            @RequestParam(required = false, defaultValue = "0") int start,
            @RequestParam(required = false, defaultValue = "15") int limit,
            @RequestParam(required = false, defaultValue = "popular") String sortKey) {
        return cardSearchService.search(key, value, start, limit, sortKey);
    }

    /**
     * 创建文字卡(POST)
     * @param title 标题
     * @param text 文字
     * @param topics 话题名数组
     * @return JSON
     */
    @PostMapping("/create/text")
    public Map<String, Object> createTxt(String title, String text, String[] topics) {
        return cardService.createCard(title, text, Card.TEXT_CARD, topics);
    }

    /**
     * 创建QUILL卡(POST)
     * @param title 标题
     * @param text 文字
     * @param topics 话题名数组
     * @return JSON
     */
    @PostMapping("/create/quill")
    public Map<String, Object> createQuill(String title, String text, String[] topics) {
        return cardService.createCard(title, text, Card.QUILL_CARD, topics);
    }

    /**
     * 创建分享卡(POST)
     * @param text 文字
     * @param parentId 分享卡片ID
     * @return JSON
     */
    @PostMapping("/create/share")
    public Map<String, Object> createShare(String text, String parentId) {
        return cardService.createShareCard(text, Card.SHARE_CARD, parentId);
    }

    /**
     * 创建图片卡(POST)
     * @param title 标题
     * @param text 文字
     * @param topics 话题名数组
     * @param remarks 图片备注数组
     * @param files 图片
     * @return JSON
     */
    @PostMapping("/create/image")
    public Map<String, Object> createImage(
            String title, String text, String[] topics,
            String[] remarks, MultipartFile[] files) {
        return cardService.createImageCard(title, text, Card.IMAGE_CARD, topics, remarks, files);
    }

    /**
     * 创建视频卡(POST)
     * @param title 标题
     * @param text 文字
     * @param topics 话题名数组
     * @param fileName 视频备注
     * @param file 视频
     * @return JSON
     */
    @PostMapping("/create/video")
    public Map<String, Object> createVideo(String title, String text, String[] topics,
            String fileName, MultipartFile file) {
        return cardService.createVideoCard(title, text, Card.VIDEO_CARD, topics,
                fileName, file);
    }

    /**
     * 创建音频卡(POST)
     * @param title 标题
     * @param text 文字
     * @param topics 话题名
     * @param fileName 音频备注
     * @param file 音频
     * @return JSON
     */
    @PostMapping("/create/voice")
    public Map<String, Object> createVoice(String title, String text, String[] topics,
            String fileName, MultipartFile file) {
        return cardService.createVoiceCard(title, text, Card.VOICE_CARD, topics,
                fileName, file);
    }

    /**
     * 卡片删除（POST）
     * @param cardId 卡片ID
     * @return JSON
     */
    @PostMapping("/del")
    public Map<String, Object> del(@RequestParam String cardId) {
        return cardService.delCard(cardId);
    }
}
