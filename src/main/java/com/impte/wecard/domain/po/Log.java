package com.impte.wecard.domain.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.*;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Log implements Serializable {

    private static final long serialVersionUID = 60673367507876658L;

    private String id;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp operateTime;
    private String status;
    private String address;
    private User operator;

    public Log(String id, String description, String address, User operator) {
        this.id = id;
        this.description = description;
        this.address = address;
        this.operator = operator;
    }

}
