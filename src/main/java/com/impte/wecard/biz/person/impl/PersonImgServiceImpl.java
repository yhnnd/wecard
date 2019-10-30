package com.impte.wecard.biz.person.impl;

import com.impte.wecard.biz.ConstantCollection;
import com.impte.wecard.biz.person.PersonImgService;
import com.impte.wecard.biz.tool.ImgDeleteService;
import com.impte.wecard.biz.tool.ImgUploadService;
import com.impte.wecard.dao.LogDao;
import com.impte.wecard.dao.UserDao;
import com.impte.wecard.domain.po.Log;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.Address;
import com.impte.wecard.utils.CurrentUtil;
import com.impte.wecard.utils.IP;
import com.impte.wecard.utils.UUID;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PersonImgServiceImpl implements PersonImgService {

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${qiniu.avatar}")
    private String qiniuAvatar;

    @Value("${qiniu.domain}")
    private String qiniuDomain;

    @Value("${qiniu.styleImg}")
    private String qiniuStyleImg;

    private final ImgUploadService imgUploadService;
    private final ImgDeleteService imgDeleteService;
    private final UserDao userDao;
    private final LogDao logDao;

    public PersonImgServiceImpl(ImgUploadService imgUploadService,
                                ImgDeleteService imgDeleteService,
                                UserDao userDao, LogDao logDao) {
        this.imgUploadService = imgUploadService;
        this.imgDeleteService = imgDeleteService;
        this.userDao = userDao;
        this.logDao = logDao;
    }

    @Override
    public Map<String, String> setAvatar(MultipartFile avatar) throws IOException {
        String message;
        Map<String, String> map = new HashMap<>();
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (avatar == null || avatar.getSize() == 0) {
            message = "Avatar cannot be empty";
        }else if (Objects.requireNonNull(avatar.getOriginalFilename()).length() == 0){
            message = "Filename cannot be empty";
        }else {
            String userId = user.getId();

            String imgName = avatar.getOriginalFilename();
            String postfix = imgName.substring(imgName.lastIndexOf("."), imgName.length());
            if (!ConstantCollection.ALLOW_IMG_SUFFIX.contains(postfix)){
                message = "Format error";
            }else {
                //1--服务器存放文件的地址
                String severPath = uploadPath;
                //2--文件名生成
                String newImgName = System.currentTimeMillis() + "_avatar" + postfix;
                //3--服务器地址加名字
                String severFilePathName = severPath + File.separator + newImgName;
                //4--上传文件
                File serverFile = new File(severFilePathName);
                avatar.transferTo(serverFile);
                //先删除数据库中的头像，如果是默认则不删除
                String dataAvatarUrl = userDao.findAvatarById(userId);
                String[] dataAvatar = dataAvatarUrl.split("/");
                String dataAvatarName = dataAvatar[dataAvatar.length - 1];
                if (!dataAvatarName.equals(qiniuAvatar)){
                    imgDeleteService.ImgDelete(dataAvatarName);
                }
                //6--上传文件到七牛云
                String key = imgUploadService.imageUpload(severFilePathName, newImgName);
                String avatarUrl = qiniuDomain + key;
                if (key == null){
                    message = "Set fail";
                    serverFile.delete();
                }else{
                    int result = userDao.setAvatar(user.getId(), avatarUrl);
                    if (result == 0){
                        message = "Set fail";
                        serverFile.delete();
                    }else {
                        map.put("avatarUrl", avatarUrl);
                        message = "Set success";
                        serverFile.delete();

                        //插入修改头像日志
                        logDao.insert(
                                new Log(
                                        UUID.getUUID(),
                                        "修改头像",
                                        Address.getAddressByIP(IP.getClientIpAddress(
                                            Objects.requireNonNull(CurrentUtil.getRequest()))),
                                        user
                                )
                        );
                    }
                }
            }
        }
        map.put("message", message);

        return map;
    }

    @Override
    public Map<String, String> setStyleImg(MultipartFile styleImg) throws IOException {
        String message;
        Map<String, String> map = new HashMap<>();
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (styleImg == null || styleImg.getSize() == 0) {
            message = "StyleImg cannot be empty";
        }else if (styleImg.getOriginalFilename().length() == 0){
            message = "File Name error";
        }else {
            String userId = user.getId();

            String imgName = styleImg.getOriginalFilename();
            String postfix = imgName.substring(imgName.lastIndexOf("."), imgName.length());
            if (!ConstantCollection.ALLOW_IMG_SUFFIX.contains(postfix)){
                message = "Format of styleImg error";
            }else {
                //服务器存放文件的地址
                String severPath = uploadPath;
                //文件名生成
                String newImgName = System.currentTimeMillis() + "_styleImg" + postfix;
                //服务器地址加名字
                String severFilePathName = severPath + File.separator + newImgName;
                //上传文件
                File serverFile = new File(severFilePathName);
                styleImg.transferTo(serverFile);
                //先删除数据库中的头像，如果是默认则不删除
                String dataStyleUrl = userDao.findStyleImgById(userId);
                String[] dataStyle = dataStyleUrl.split("/");
                String dataStyleName = dataStyle[dataStyle.length - 1];
                if (!dataStyleName.equals(qiniuStyleImg)){
                    imgDeleteService.ImgDelete(dataStyleName);
                }
                //上传文件到七牛云
                String key = imgUploadService.imageUpload(severFilePathName, newImgName);
                String styleImgUrl = qiniuDomain + key;
                if (key == null){
                    message = "Set fail";
                }else{
                    int result = userDao.setStyleImg(user.getId(), styleImgUrl);
                    if (result == 1){
                        message = "Set success";
                        map.put("styleImgUrl", styleImgUrl);

                        //插入修改个性图日志
                        logDao.insert(
                                new Log(
                                        UUID.getUUID(),
                                        "修改个性图",
                                        Address.getAddressByIP(IP.getClientIpAddress(CurrentUtil.getRequest())),
                                        user
                                )
                        );
                    }else {
                        message = "Set fail";
                    }
                }
            }
        }
        map.put("message", message);

        return map;
    }
}
