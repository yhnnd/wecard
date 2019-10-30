package com.impte.wecard.domain.po;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author justZero
 * @since 2018/3/6
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Voice implements Serializable {

    private static final long serialVersionUID = 4075666176532335351L;

    public static final int NAME_MAX_LEN = 10;

    private String id;
    private String url;
    private String name;
    private String createdTime;

    public Voice(String id) {
        this.id = id;
    }

}
