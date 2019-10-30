package com.impte.wecard.domain.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.*;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Room implements Serializable {

    private static final long serialVersionUID = -7071797595465801060L;

    private String id;
    private String roomName;
    private String roomImgUrl;
    private Integer membersNum;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp roomCreateTime;
    private String status;
    private User owner;
    private Set<Message> roomMessages;
    private Set<Request> requests;
    private Set<RoomMember> roomMembers;

    public Room(String id) {
        this.id = id;
    }

    public Room(String id, String roomName, String roomImgUrl, Timestamp roomCreateTime, String status) {
        this.id = id;
        this.roomName = roomName;
        this.roomImgUrl = roomImgUrl;
        this.roomCreateTime = roomCreateTime;
        this.status = status;
    }

}
