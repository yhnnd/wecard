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
public class Message implements Serializable {

    private static final long serialVersionUID = 953044819699878245L;

    private String id;
    private String text;
    private String imgUrl;
    private String type;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp messageCreateTime;
    private int recall;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp recallTime;
    private Video video;
    private Voice voice;
    private User user;
    private RoomMember roomMember;//数据库中没有这个字段，用于返回群消息时作为群成员的载体
    private Friend friend;
    private Room room;
    private Set<User> removeItUsers;

    public Message(String id) {
        this.id = id;
    }

    //发送给朋友的文本或者图片消息
    public Message(String id, String text, String imgUrl, String type, User user, Friend friend) {
        this.id = id;
        this.text = text;
        this.imgUrl = imgUrl;
        this.type = type;
        this.user = user;
        this.friend = friend;
    }

    //群发文本或者图片消息
    public Message(String id, String text, String imgUrl, String type, User user, Room room) {
        this.id = id;
        this.text = text;
        this.imgUrl = imgUrl;
        this.type = type;
        this.user = user;
        this.room = room;
    }

}
