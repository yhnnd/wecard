package com.impte.wecard.domain.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.*;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author justZero
 * @since 2018/3/5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Card implements Serializable {

    private static final long serialVersionUID = -1220456359302572303L;

    /**
     * 状态
     */
    public static final String DELETE = "delete";
    public static final String EXIST = "exist";

    /**
     * 字数限制
     */
    public static final Integer SUMMARY_WORAD_NUM = 66;
    public static final Integer TITLE_MAX_LEN = 32;
    public static final Integer TEXT_MAX_LEN = 2000;
    /**
     * 卡片类型
     */
    public static final String TEXT_CARD = "text";
    public static final String QUILL_CARD = "quill";
    public static final String IMAGE_CARD = "image";
    public static final String VIDEO_CARD = "video";
    public static final String VOICE_CARD = "voice";
    public static final String SHARE_CARD = "share";

    private String id;
    private String title;
    private String text;
    private Integer cardHot;
    private Integer shareNum;
    private Integer commentNum;
    private List<Topic> topics;
    private List<CardImage> images;
    private List<Comment> comments;
    private Video video;
    private Voice voice;
    /**
     * 五种卡片
     * text, image, video, voice, share
     */
    private String type;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp createdTime;

    /**
     * exist：卡片存在
     * delete：卡片被标记删除
     */
    private String status = EXIST;
    /**
     * 转发的那个卡片
     */
    private Card share;
    private User user;

    /**
     * 是否点过赞，用于判断当前用户是否已经点赞，非空则已经点赞
     */
    private String iLike;

    public Card(String id, String text, String type) {
        this.id = id;
        this.text = text;
        this.type = type;
    }

    public Card(String id, String type) {
        this.id = id;
        this.type = type;
    }

    public Card(String id) {
        this.id = id;
    }

    public Card(String id, String title, String text, String type) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.type = type;
    }

}
