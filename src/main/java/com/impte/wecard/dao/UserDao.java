package com.impte.wecard.dao;

import com.impte.wecard.domain.po.User;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends GenericDao<User, String> {
    String verifyPassword(@Param("verifyName") String verifyName,
        @Param("password") String password);
    User verifyUserExistByUsername(String username);
    User verifyUserExistByPhone(String phone);
    User verifyUserExistById(String id);
    void sp_affairs_before_login(String id);
    User loginGetUser(String id);
    int registerUser(User user);
    int newQQUser(User user);
    String checkUserName(String username);
    String checkPhoneNumber(String phoneNumber);
    User findPhoneByAccount(String account);
    int setLogoutTime(String id);
    int alterPassword(@Param("id") String id, @Param("password") String password);
    int setAvatar(@Param("id") String id, @Param("avatarUrl") String avatarUrl);
    int setStyleImg(@Param("id") String id, @Param("styleImgUrl") String styleImgUrl);
    int setNickname(@Param("id") String id, @Param("nickname") String nickname);
    int setSignature(@Param("id") String id, @Param("signature") String signature);
    int setGender(@Param("id") String id, @Param("gender") String gender);
    int setCity(@Param("id") String id, @Param("city") String city);
    int alterPhone(@Param("id") String id, @Param("phone") String phoneNumber);
    int deleteUser(String id);
    List<User> findRoomMembers(String roomId);
    User lookUser(String username);
    int findUserGrade(String id);
    List<User> findUsersByUsername(
        @Param("username") String username,
        @Param("offset") int offset,
        @Param("limit") int limit
    );
    String findAvatarById(String userId);
    String findStyleImgById(String userId);
    User findSimpleUser(String userId);
}
