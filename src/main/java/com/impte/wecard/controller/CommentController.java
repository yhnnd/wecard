package com.impte.wecard.controller;

import com.impte.wecard.biz.card.CommentService;
import com.impte.wecard.domain.po.Card;
import com.impte.wecard.domain.po.Comment;
import com.mysql.jdbc.StringUtils;
import java.util.Map;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author justZero
 * @since 2018/3/9
 */
@RestController
@CrossOrigin
@AllArgsConstructor
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    /**
     * 一级评论(GET)
     * <p>包含二级评论
     * @param id 卡片ID
     * @param start 评论起始，默认0
     * @param limit 评论条数，默认5
     * @param sortKey 排序方式：poplar|time(default)
     * @return JSON
     */
    @GetMapping("/load")
    public Map<String, Object> loadCmnts(
            @RequestParam("cardId") String id,
            @RequestParam(required = false, defaultValue = "0") int start,
            @RequestParam(required = false, defaultValue = "5") int limit,
            @RequestParam(required = false, defaultValue = "time") String sortKey) {
        return commentService.getCmntsByCardId(id, start, limit, sortKey);
    }

    /**
     * 加载一级评论(GET)
     * @param id 卡片ID
     * @param start 评论起始，默认0
     * @param limit 评论条数，默认5
     * @param sortKey 排序方式：popular|time(default)
     * @return JSON
     */
    @GetMapping("/lv1")
    public Map<String, Object> loadLv1Cmnts(
            @RequestParam("carId") String id,
            @RequestParam(required = false, defaultValue = "0") int start,
            @RequestParam(required = false, defaultValue = "5") int limit,
            @RequestParam(required = false, defaultValue = "time") String sortKey) {
        return commentService.getLv1Cmnts(id, start, limit, sortKey);
    }

    /**
     * 加载二级评论(GET)
     * @param id 评论ID
     * @param start 评论起始，默认0
     * @param limit 评论条数，默认5
     * @return JSON
     */
    @GetMapping("/lv2")
    public Map<String, Object> loadLv2Cmnts(
            @RequestParam("commentId") String id,
            @RequestParam(required = false, defaultValue = "0") int start,
            @RequestParam(required = false, defaultValue = "5") int limit ) {
        return commentService.getLv2Cmnts(id, start, limit);
    }

    /**
     * 加载用户所有的评论（GET）
     * @return JSON
     */
    @GetMapping("/mine")
    public Map<String, Object> loadUserCmnts(
            @RequestParam(required = false, defaultValue = "0") int start,
            @RequestParam(required = false, defaultValue = "15") int limit) {
        return commentService.getUserCmnt(start, limit);
    }

    /**
     * 加载回复用户的评论（GET）
     * @param start 评论起始，默认0
     * @param limit 评论条数，默认15
     * @return JSON
     */
    @GetMapping("/tome")
    public Map<String, Object> loadToUserCmnts(
            @RequestParam(required = false, defaultValue = "0") int start,
            @RequestParam(required = false, defaultValue = "15") int limit) {
        return commentService.getCmntToUser(start, limit);
    }

    /**
     * 评论点赞(POST)
     * @param commentId 评论ID
     * @return JSON
     */
    @PostMapping("/like")
    public Map<String, Object> likeCmnt(String commentId) {
        return commentService.userLikeCmnt(commentId);
    }

    /**
     * 评论取消点赞(POST)
     * @param commentId 评论ID
     * @return JSON
     */
    @PostMapping("/unlike")
    public Map<String, Object> unlikeCmnt(@RequestParam String commentId) {
        return commentService.userUnlikeCmnt(commentId);
    }

    /**
     * 创建评论(POST)
     * @param text 评论内容
     * @param cardId 卡片ID
     * @param parentId 回复评论的ID（非必须）
     * @return JSON
     */
    @PostMapping("/create")
    public Map<String, Object> createCmnt(
            @RequestParam("text") String text,
            @RequestParam("cardId") String cardId,
            @RequestParam(required = false, defaultValue = "") String parentId) {
        if (StringUtils.isNullOrEmpty(parentId)) {
            parentId = null;
        }
        Comment comment = new Comment(text, new Card(cardId), new Comment(parentId));
        return commentService.createCmnt(comment);
    }

    /**
     * 评论删除(POST)
     * @param commentId 评论ID
     * @return JSON
     */
    @PostMapping("/del")
    public  Map<String, Object> delCmnt(String commentId) {
        return commentService.delCmnt(commentId);
    }

}
