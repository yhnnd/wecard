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
public class ChatItem implements Serializable {

    private static final long serialVersionUID = 5407410245734049672L;

    private String id;
    private String type;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp updateTime;
    private Integer unreadNum;
    private Integer visible;
    private Message message;
    private Request request;
    private Friend friend;
    private Room room;
    private SystemNotification systemNotification;
    private User user;

    //加好友成功，插入chatItem
    public ChatItem(String id, String type, Message message, Friend friend, User user) {
        this.id = id;
        this.type = type;
        this.message = message;
        this.friend = friend;
        this.user = user;
    }

}
