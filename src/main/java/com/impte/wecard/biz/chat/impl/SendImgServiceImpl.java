package com.impte.wecard.biz.chat.impl;

import com.impte.wecard.biz.ConstantCollection;
import com.impte.wecard.biz.chat.OnlineMsgService;
import com.impte.wecard.biz.chat.SendImgService;
import com.impte.wecard.biz.tool.ImgUploadService;
import com.impte.wecard.dao.ChatItemDao;
import com.impte.wecard.dao.FriendDao;
import com.impte.wecard.dao.MessageDao;
import com.impte.wecard.dao.UserDao;
import com.impte.wecard.domain.po.ChatItem;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import com.impte.wecard.utils.UUID;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SendImgServiceImpl implements SendImgService {

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${qiniu.domain}")
    private String qiniuDomain;

    private final ChatItemDao chatItemDao;

    private final FriendDao friendDao;

    private final ImgUploadService imgUploadService;

    private final DataSourceTransactionManager txManager;

    private final OnlineMsgService onlineMsgService;

    private final MessageDao messageDao;

    private final UserDao userDao;

    public SendImgServiceImpl(ChatItemDao chatItemDao, FriendDao friendDao,
                              ImgUploadService imgUploadService, DataSourceTransactionManager txManager,
                              OnlineMsgService onlineMsgService, MessageDao messageDao, UserDao userDao) {
        this.chatItemDao = chatItemDao;
        this.friendDao = friendDao;
        this.imgUploadService = imgUploadService;
        this.txManager = txManager;
        this.onlineMsgService = onlineMsgService;
        this.messageDao = messageDao;
        this.userDao = userDao;
    }

    private String sendImgToFriend(User sender, String friendId, String friendRoomId, MultipartFile image, String fileType) throws IOException {
        String result;

        String imgName = image.getOriginalFilename();
        String postfix = imgName.substring(imgName.lastIndexOf("."), imgName.length());
        //3--后缀分类处理
        if (!ConstantCollection.ALLOW_IMG_SUFFIX.contains(postfix)){
            result = "Format of image error";
        }else {
            String messageId = UUID.getUUID();
            //服务器存放文件的地址
            String severPath = uploadPath;
            //4--生成图片名
            String newImgName = messageId + "_messageImg" + postfix;
            //服务器地址加名字
            String severFilePathName = severPath + File.separator + newImgName;
            //上传文件
            File serverFile = new File(severFilePathName);
            image.transferTo(serverFile);
            //上传文件到七牛云
            String key = imgUploadService.imageUpload(severFilePathName, newImgName);
            String imgUrl = qiniuDomain + key;

            if (key == null){
                result = "Set fail";
            }else{
                DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);// 事物隔离级别，开启新事务
                TransactionStatus status = txManager.getTransaction(def);// 获得事务状态
                try{
                    //插入message
                    messageDao.insertFriendBasicMsg(
                            messageId,
                            null,
                            imgUrl,
                            fileType,
                            sender.getId(),
                            friendRoomId
                    );
                    //获取我自己的对应的聊天栏
                    ChatItem meToFriendChatItem = chatItemDao.findByObjectId(sender.getId(), friendId);
                    //获取好友的对应的聊天栏
                    ChatItem friendToMeChatItem = chatItemDao.findByObjectId(friendId, sender.getId());
                    //更新自己数据库的聊天栏
                    chatItemDao.updateTimeAndMsgId(meToFriendChatItem.getId(), messageId);
                    //更新好友数据库的聊天栏
                    chatItemDao.updateTimeAndMsgId(friendToMeChatItem.getId(), messageId);
                    onlineMsgService.sendToFriend(
                            friendId,
                            friendToMeChatItem.getId(),
                            messageId,
                            "load_new_message"
                    );
                    //发送消息给自己
                    onlineMsgService.sendToSelf(
                            sender.getId(),
                            meToFriendChatItem.getId(),
                            messageId,
                            "load_new_message"
                    );

                    result = "Send success";
                    //事务提交
                    txManager.commit(status);
                }catch(Exception e){
                    result = "Send Fail";
                    //事务回滚
                    txManager.rollback(status);
                }
            }
        }
        return result;
    }

    private String sendImgToRoom(User sender, String roomId, MultipartFile image, String fileType) throws IOException {
        String result;

        String imgName = image.getOriginalFilename();
        String postfix = imgName.substring(imgName.lastIndexOf("."), imgName.length());
        //3--后缀分类处理
        if (!ConstantCollection.ALLOW_IMG_SUFFIX.contains(postfix)){
            result = "Format of image error";
        }else {
            String messageId = UUID.getUUID();
            //1--服务器存放文件的地址
            String severPath = uploadPath;
            //2--生成图片名
            String newImgName = messageId + "_messageImg" + postfix;
            //3--服务器地址加名字
            String severFilePathName = severPath + File.separator + newImgName;
            //4--上传文件
            File serverFile = new File(severFilePathName);
            image.transferTo(serverFile);
            //5--上传文件到七牛云
            String key = imgUploadService.imageUpload(severFilePathName, newImgName);
            String imgUrl = qiniuDomain + key;
            if (key == null) {
                result = "Send fail";
            } else {
                List<User> members = userDao.findRoomMembers(roomId);
                if (members == null || members.size() == 0) {
                    result = "Send Fail";
                } else {
                    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);// 事物隔离级别，开启新事务
                    TransactionStatus status = txManager.getTransaction(def);// 获得事务状态
                    try {
                        messageDao.insertRoomBasicMsg(
                                messageId,
                                null,
                                imgUrl,
                                fileType,
                                sender.getId(),
                                roomId
                        );
                        //获取我自己的对应的聊天栏
                        ChatItem meToRoomChatItem = chatItemDao.findByObjectId(sender.getId(), roomId);
                        //更新自己数据库的聊天栏
                        chatItemDao.updateTimeAndMsgId(meToRoomChatItem.getId(), messageId);
                        for (User member : members) {
                            //获取成员的对应的聊天栏
                            ChatItem memberToRoomChatItem = chatItemDao.findByObjectId(member.getId(), roomId);
                            //更新好友数据库的聊天栏
                            chatItemDao.updateTimeAndMsgId(memberToRoomChatItem.getId(), messageId);
                            //发送消息给单个群成员
                            onlineMsgService.sendToFriend(
                                    member.getId(),
                                    memberToRoomChatItem.getId(),
                                    messageId,
                                    "load_new_message"
                            );
                        }
                        //发送消息给自己
                        onlineMsgService.sendToSelf(
                                sender.getId(),
                                meToRoomChatItem.getId(),
                                messageId,
                                "load_new_message"
                        );

                        //设置result为发送成功
                        result = "Send success";
                        //事务提交
                        txManager.commit(status);
                    } catch (Exception e) {
                        result = "Send Fail";
                        //事务回滚
                        txManager.rollback(status);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public String sendImgToChatItem(String chatItemId, MultipartFile image, String fileType) throws IOException {
        String result;

        User sender = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        if (chatItemId == null || chatItemId.equals("")){
            result = "ChatItemId cannot be empty";
        }else if (image == null || image.getSize() == 0) {
            result = "Image cannot be empty";
        }else if (image.getOriginalFilename().length() == 0){
            result = "Image Name error";
        }else if (!fileType.equals("image")){
            result = "Image type error";
        }else {
            //验证chatItem是否属于此人
            ChatItem chatItem = chatItemDao.verifyChatItem(chatItemId);
            if (chatItem == null || !chatItem.getUser().getId().equals(sender.getId())){
                result = "ChatItem does not exist";
            }else{
                String friendRoomId = friendDao.findFriendsRoomId(sender.getId(), chatItem.getFriend().getId());
                if (friendRoomId == null){
                    result = "friend does not exist";
                }else if (chatItem.getType().equals("friend")){
                    String friendId = chatItem.getFriend().getId();
                    result = sendImgToFriend(sender, friendId, friendRoomId, image, fileType);
                }else if (chatItem.getType().equals("room")){
                    String roomId = chatItem.getRoom().getId();
                    result = sendImgToRoom(sender, roomId, image, fileType);
                }else {
                    result = "ChatItem type error";
                }
            }
        }
        return result;
    }
}
