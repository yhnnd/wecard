package com.impte.wecard.biz.user;

public interface FollowService {
    String follow(String followingId);
    String unFollow(String followingId);
}
