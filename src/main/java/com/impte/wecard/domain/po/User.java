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
public class User implements Serializable {

    private static final long serialVersionUID = -6939503621735328753L;

    private String id;
    private String username;
    private String password;
    private String phone;
    private String nickname;
    private String gender;
    private String city;
    private Integer grade;
    private String signature;
    private String avatarUrl;
    private String styleImgUrl;
    private Integer fansNum;
    private Integer followNum;
    private Integer cardsNum;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp userCreateTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp lastLoginTime;
    private Set<Card> personalCards;
    private Set<Comment> personalComments;
    private Set<User> fans;
    private Set<User> followees;
    private Set<Group> groups;
    private Set<Card> likeCards;
    private Set<Log> logs;
    private Set<Message> userMessages;
    private Set<Photo> photos;
    private Set<Message> removedMessages;
    private Set<Request> sendRequests;
    private Set<Request> receiveRequests;
    private Set<RoomMember> asRoomMembers;
    private Set<Topic> topics;
    private Set<ChatItem> chatItems;

    public User(String id) {
        this.id = id;
    }

    //注册
    public User(String id, String username, String password, String phone, String nickname, String city) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.nickname = nickname;
        this.city = city;
    }

    public User(String id, String username, String nickname, String avatarUrl) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
    }

    //新加qq用户
    public User(String id, String username, String password, String phone, String nickname, String gender, String city, String avatarUrl) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.nickname = nickname;
        this.gender = gender;
        this.city = city;
        this.avatarUrl = avatarUrl;
    }

    //除去Set
    public User(String id, String username, String password, String phone, String nickname, String city, String gender, Integer grade, String signature, String avatarUrl, String styleImgUrl, Integer fansNum, Integer followNum, Integer cardsNum, Timestamp userCreateTime, Timestamp lastLoginTime) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.nickname = nickname;
        this.city = city;
        this.gender = gender;
        this.grade = grade;
        this.signature = signature;
        this.avatarUrl = avatarUrl;
        this.styleImgUrl = styleImgUrl;
        this.fansNum = fansNum;
        this.followNum = followNum;
        this.cardsNum = cardsNum;
        this.userCreateTime = userCreateTime;
        this.lastLoginTime = lastLoginTime;
    }

}