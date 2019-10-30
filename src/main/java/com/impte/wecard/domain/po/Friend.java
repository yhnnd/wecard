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
public class Friend implements Serializable {

    private static final long serialVersionUID = 134833540973386463L;

    private String id;
    private String username;
    private String nickname;
    private String gender;
    private String city;
    private Integer grade;
    private String avatarUrl;
    private String styleImgUrl;
    private Integer fansNum;
    private Integer followNum;
    private Integer cardsNum;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp createFriendTime;
    private Group group;
    private User user;

}
