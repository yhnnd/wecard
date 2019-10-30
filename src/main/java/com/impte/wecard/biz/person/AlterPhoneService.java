package com.impte.wecard.biz.person;

public interface AlterPhoneService {
    String sendVerCode(String newPhoneNumber);
    String alterPhone(String verCode);
}
