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
public class Video implements Serializable {

    private static final long serialVersionUID = -3907908955226603981L;

    public static final int NAME_MAX_LEN = 10;

    private String id;
    private String url;
    private String imgUrl;
    private String name;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp createdTime;

    public Video(String id) {
        this.id = id;
    }
}
