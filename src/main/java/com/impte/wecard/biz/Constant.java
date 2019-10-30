package com.impte.wecard.biz;

/**
 * @author xutong44
 */
public interface Constant {

  int MAX_USERNAME_LENGTH = 12;
  String MESSAGE = "message";
  String USER = "user";

  String USERNAME_CANNOT_BE_EMPTY = "Username cannot be empty";
  String USERNAME_IS_TOO_LONG = "Username is too long";
  String USERNAME_CONTAINS_SPECIAL_CHAR =  "Username contains special char";
  String USERNAME_ALREADY_EXISTS = "Username already exists";
  String USERNAME_IS_AVAILABLE = "Username is available";

  String PHONE_NUMBER_CANNOT_BE_EMPTY = "Phone number cannot be empty";
  String INCORRECT_PHONE_NUMBER = "Incorrect phone number format";
  String PHONE_NUMBER_ALREADY_EXISTS = "Phone number already exists";
  String PHONE_NUMBER_IS_AVAILABLE = "Phone number is available";

  String THE_ACCOUNT_DOES_NOT_EXIST = "The account does not exist";
}
