package com.impte.wecard.domain.po;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class RoomMember implements Serializable {

    private static final long serialVersionUID = 4785052881219667845L;

    private String id;
    private String username;
    private String nickname;
    private String gender;
    private String city;
    private Integer grade;
    private String avatarUrl;
    private Integer fansNum;
    private Integer followNum;
    private Integer cardsNum;
    private String role;
    private String roomRemark;
    private String joinTime;
    private Room room;

}
