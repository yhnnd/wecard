package com.impte.wecard.domain.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.*;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author justZero
 * @since 2018/3/6
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Topic implements Serializable {

    private static final long serialVersionUID = 8881878474130368028L;

    /**
     * 卡片话题最大数3个
     */
    public static final int MAX_NUM = 3;

    public static final int NAME_MAX_LEN = 6;

    private String id;
    private String name;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp createdTime;
    private User creator;

    public Topic(String id, String name, User creator) {
        this.id = id;
        this.name = name;
        this.creator = creator;
    }

}
