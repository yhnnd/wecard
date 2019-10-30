package com.impte.wecard.domain.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.*;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Comment implements Serializable {

    private static final long serialVersionUID = -5240294744262395546L;

    /**
     * 字数限制
     */
    public static final Integer TEXT_MAX_LEN = 140;

    /**
     * 评论的状态
     */
    public static final String EXIST = "exist";
    public static final String DELETE = "delete";

    private String id;
    private String text;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp createdTime;
    private Integer commentHot = 0;
    private String status = EXIST;
    private Card card;
    private User user;
    private Comment parent;
    private List<Comment> children;

    public Comment(String id) {
        this.id = id;
    }

    public Comment(String text, Card card, Comment parent) {
        this.text = text;
        this.card = card;
        this.parent = parent;
    }

}
