package com.impte.wecard.biz.begin;

public interface RegisterService {
    String sendVerCode(String phoneNumber);
    String registerService(String username, String password, String phoneNumber, String verCode);
}
