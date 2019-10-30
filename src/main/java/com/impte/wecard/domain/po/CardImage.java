package com.impte.wecard.domain.po;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.*;

import java.io.Serializable;

/**
 * @author justZero
 * @since 2018/3/6
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class CardImage implements Serializable {

    private static final long serialVersionUID = 4386364368415109528L;

    /**
     * 图片卡最多6张图
     */
    public static final int MAX_NUM = 6;
    public static final int REMARK_MAX_LEN = 10;

    private String id;
    private String url;
    private String remark;
    private Card card;

    public CardImage(String id, Card card) {
        this.id = id;
        this.card = card;
    }

}
