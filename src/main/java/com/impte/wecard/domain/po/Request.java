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
public class Request implements Serializable {

    private static final long serialVersionUID = -2396860704717057568L;

    private String id;
    private String type;
    private String requestMessage;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp requestTime;
    private String requestStatus;
    private String requestRemark;
    private Group requestGroup;
    private User requester;
    private User receiver;
    private User replier;
    private Room room;

}
