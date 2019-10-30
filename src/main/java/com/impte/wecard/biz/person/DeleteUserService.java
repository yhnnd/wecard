package com.impte.wecard.biz.person;

public interface DeleteUserService {
    String sendVerCode();
    String deleteUser(String verCode);
}
