package com.impte.wecard.biz.card.impl;

import com.impte.wecard.biz.ConstantCollection;
import com.impte.wecard.biz.card.CardService;
import com.impte.wecard.biz.tool.ImgDeleteService;
import com.impte.wecard.biz.tool.ImgUploadService;
import com.impte.wecard.dao.*;
import com.impte.wecard.domain.po.*;
import com.impte.wecard.utils.CurrentUtil;
import com.impte.wecard.utils.UUID;
import com.impte.wecard.utils.web.ResponseMessage;
import com.mysql.jdbc.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author justZero
 * @since 2018/3/6
 */
@Service
public class CardServiceImpl implements CardService {

    @Value("${qiniu.domain}")
    private String qiniuDomain;

    @Value("${upload.path}")
    private String uploadPath;

    private final CardDao cardDao;

    private final TopicDao topicDao;

    private final CommentDao commentDao;

    private final VideoDao videoDao;

    private final VoiceDao voiceDao;

    private final CardImageDao imageDao;

    private final ImgUploadService imgUploadService;

    private final ImgDeleteService imgDeleteService;

    private final DataSourceTransactionManager txManager;

    public CardServiceImpl(CardDao cardDao, TopicDao topicDao, CommentDao commentDao,
                           VideoDao videoDao, VoiceDao voiceDao, CardImageDao imageDao,
                           ImgUploadService imgUploadService, ImgDeleteService imgDeleteService,
                           DataSourceTransactionManager txManager) {
        this.cardDao = cardDao;
        this.topicDao = topicDao;
        this.commentDao = commentDao;
        this.videoDao = videoDao;
        this.voiceDao = voiceDao;
        this.imageDao = imageDao;
        this.imgUploadService = imgUploadService;
        this.imgDeleteService = imgDeleteService;
        this.txManager = txManager;
    }

    @Override
    public Map<String, Object> getCardPreviews(int start, int limit, String sortKey) {
        Map<String, Object> response = new HashMap<>(2);

        try {
            User user = CurrentUtil.getUser();
            List<Card> cards = cardDao.findPreviews(start, limit, sortKey,
                user == null ? null : user.getId()
            );
            if (cards.size() == 0) {
                response.put("message", ResponseMessage.NO_DATA);
                return response;
            }
            this.textToSummary(cards);
            response.put("cards", cards);
            response.put("message", ResponseMessage.CARD_LOAD_SUCCESS);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", ResponseMessage.CARD_LOAD_FAILED);
            return response;
        }
    }

    @Override
    public Map<String, Object> getUserCardPreviews(int start, int limit, String sortKey) {
        Map<String, Object> response = new HashMap<>(2);

        User user = CurrentUtil.getUser();
        // 已经过登录状态检查拦截器鉴权
        try {
            List<Card> cards = cardDao.findByUserId(user.getId(), start, limit, sortKey, user.getId());
            if (cards.size() == 0) {
                response.put("message", ResponseMessage.NO_DATA);
                return response;
            }
            this.textToSummary(cards);
            response.put("cards", cards);
            response.put("message", ResponseMessage.CARD_LOAD_SUCCESS);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", ResponseMessage.CARD_LOAD_FAILED);
            return response;
        }
    }

    @Override
    public Map<String, Object> getLikedCardPreviews(int start, int limit) {
        Map<String, Object> response = new HashMap<>(2);

        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权
        try {
            List<Card> likedCards = cardDao.findLikedByUserId(user.getId(), start, limit, user.getId());
            if (likedCards.size() == 0) {
                response.put("message", ResponseMessage.NO_DATA);
                return response;
            }
            textToSummary(likedCards);
            response.put("cards", likedCards);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", ResponseMessage.CARD_LOAD_FAILED);
            return response;
        }
        response.put("message", ResponseMessage.CARD_LOAD_SUCCESS);
        return response;
    }

    @Override
    public Map<String, Object> getSharedCardPreviews(int start, int limit) {
        Map<String, Object> response = new HashMap<>(2);

        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        try {
            List<Card> sharedCards = cardDao.findSharedByUserId(user.getId(), start, limit, user.getId());
            if (sharedCards.size() == 0) {
                response.put("message", ResponseMessage.NO_DATA);
                return response;
            }
            textToSummary(sharedCards);
            response.put("cards", sharedCards);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", ResponseMessage.CARD_LOAD_FAILED);
            return response;
        }
        response.put("message", ResponseMessage.CARD_LOAD_SUCCESS);
        return response;
    }

    @Override
    public Map<String, Object> getCardById(
            String cardId, Integer start, Integer limit, String sortKey) {
        Map<String, Object> response = new HashMap<>(2);
        try {
            User user = CurrentUtil.getUser();
            // 查询卡片数据集
            Card card = cardDao.findOne(cardId, user == null ? null : user.getId());
            // 查询卡片一级评论（包含二级评论）
            List<Comment> comments = commentDao.findByCardId(cardId, start, limit, sortKey);
            response.put("card", card);
            response.put("comments", comments);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", ResponseMessage.CARD_LOAD_FAILED);
            return response;
        }
        response.put("message", ResponseMessage.CARD_LOAD_SUCCESS);
        return response;
    }

    @Override
    public Map<String, Object> getShareCardById(String cardId, Integer start, Integer limit, String sortKey) {
        Map<String, Object> response = new HashMap<>(2);

        // 已经过登录状态检查拦截器鉴权

        // 必须是转发卡片
        if (!isSharedCard(cardId)) {
            response.put("message", ResponseMessage.IS_NOT_SHARED_CARD);
            return response;
        }

        try {
            User user = CurrentUtil.getUser();
            Card card = cardDao.findShareById(cardId, user == null ? null : user.getId());
            List<Comment> comments = commentDao.findByCardId(cardId, start, limit, sortKey);
            response.put("card", card);
            response.put("comments", comments);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", ResponseMessage.CARD_LOAD_FAILED);
            return response;
        }
        response.put("message", ResponseMessage.CARD_LOAD_SUCCESS);
        return response;
    }

    @Override
    public Map<String, Object> getFollowingCardByUserId(int start, int limit) {

        Map<String, Object> response = new HashMap<>(2);
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        try {
            List<Card> cards = cardDao.findFollowingByUserId(user.getId(), start, limit, user.getId());
            if (cards.size() == 0) {
                response.put("message", ResponseMessage.NO_DATA);
                return response;
            }
            textToSummary(cards);
            response.put("cards", cards);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", ResponseMessage.CARD_LOAD_FAILED);
            return response;
        }
        response.put("message", ResponseMessage.CARD_LOAD_SUCCESS);
        return response;
    }

    @Override
    public Map<String, Object> getUserCardsCount(String userId) {
        Map<String, Object> response = new HashMap<>(2);
        try {
            response.put("cardsCount", cardDao.countUserCards(userId));
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", ResponseMessage.ERROR);
            return response;
        }
        response.put("message", ResponseMessage.SUCCESS);
        return response;
    }

    @Override
    public Map<String, Object> getUserLikedCardsCount(String userId) {
        Map<String, Object> response = new HashMap<>(2);
        try {
            response.put("cardsCount", cardDao.countUserLikedCards(userId));
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", ResponseMessage.ERROR);
            return response;
        }
        response.put("message", ResponseMessage.SUCCESS);
        return response;
    }

    @Override
    public Map<String, Object> getUserSharedCardsCount(String userId) {
        Map<String, Object> response = new HashMap<>(2);
        try {
            response.put("cardsCount", cardDao.countUserSharedCards(userId));
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", ResponseMessage.ERROR);
            return response;
        }
        response.put("message", ResponseMessage.SUCCESS);
        return response;
    }

    @Override
    public Map<String, Object> userLikeCard(String cardId) {
        Map<String, Object> response = new HashMap<>(2);

        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        // 卡片ID不能为空
        if (StringUtils.isNullOrEmpty(cardId)) {
            response.put("message", ResponseMessage.CARD_ID_IS_EMPTY);
            return response;
        }

        // 不能重复点赞
        if (isLikedCard(cardId, user.getId())) {
            response.put("message", ResponseMessage.LIKE_CARD_REPEATED);
            return response;
        }

        // 事务
        DefaultTransactionDefinition dtd = new DefaultTransactionDefinition();
        // 事物隔离级别，开启新事务
        dtd.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        // 获得事务状态
        TransactionStatus txStatus = txManager.getTransaction(dtd);
        try {
            int res = cardDao.insertLikeCard(
                    UUID.getUUID(), cardId, user.getId());
            if (res != 1) {
                throw new RuntimeException(ResponseMessage.LIKE_CARD_FAILED);
            }
            // 事务提交
            txManager.commit(txStatus);
        } catch (Exception e) {
            e.printStackTrace();
            // 事务回滚
            txManager.rollback(txStatus);
            response.put("message", ResponseMessage.LIKE_CARD_FAILED);
            return response;
        }
        response.put("message", ResponseMessage.LIKE_CARD_SUCCESS);
        return response;
    }

    @Override
    public Map<String, Object> userUnlikeCard(String cardId) {
        Map<String, Object> response = new HashMap<>(2);

        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        // 卡片ID不能为空
        if (StringUtils.isNullOrEmpty(cardId)) {
            response.put("message", ResponseMessage.CARD_ID_IS_EMPTY);
            return response;
        }

        // 只用取消自己的点赞
        if (!isLikedCard(cardId, user.getId())) {
            response.put("message", ResponseMessage.DID_NOT_LIKE_CARD);
            return response;
        }

        DefaultTransactionDefinition dtd = new DefaultTransactionDefinition();
        dtd.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus txStatus = txManager.getTransaction(dtd);
        try {
            int res = cardDao.deleteLikeCard(cardId, user.getId());
            if (res != 1) {
                throw new RuntimeException(ResponseMessage.UNLIKE_CARD_FAILED);
            }
            txManager.commit(txStatus);
        } catch (Exception e) {
            e.printStackTrace();
            txManager.rollback(txStatus);
            response.put("message", ResponseMessage.UNLIKE_CARD_FAILED);
            return response;
        }
        response.put("message", ResponseMessage.UNLIKE_CARD_SUCCESS);
        return response;
    }

    @Override
    public Map<String, Object> createCard(String title, String text, String type, String[] topicNames) {

        Map<String, Object> response = new HashMap<>(1);
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        // 创建卡片实例：id, type
        Card card = new Card(UUID.getUUID(), type);

        if (title == null) {
            title = "";
        }
        // 卡片标题长度限制
        if (title.length() > Card.TITLE_MAX_LEN) {
            response.put("message", ResponseMessage.CARD_TITLE_TOO_LONG);
            return response;
        }
        // 设置卡片标题
        card.setTitle(title);

        if (text == null) {
            text = "";
        }
        // 卡片内容长度限制
        if (text.length() > Card.TEXT_MAX_LEN) {
            response.put("message", ResponseMessage.CARD_TEXT_TOO_LONG);
            return response;
        }
        // 设置卡片内容
        card.setText(text);

        // 保证卡片创建者为当前登录用户
        card.setUser(user);
        card.setStatus(Card.EXIST);

        // 卡片类型检测
        if (!card.getType().equals(Card.TEXT_CARD)&&!card.getType().equals(Card.QUILL_CARD)) {
            response.put("message", ResponseMessage.CARD_TYPE_ERROR);
            return response;
        }

        DefaultTransactionDefinition dtd = new DefaultTransactionDefinition();
        dtd.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus txStatus = txManager.getTransaction(dtd);
        try {
            int res = 0;

            // 话题数量限制，仅添加前三个话题
            List<Topic> topics = createTopics(topicNames, user);
            // 卡片添加话题列表
            card.setTopics(topics);

            res += cardDao.insert(card);
            if (res <= 0) {
                throw new RuntimeException(ResponseMessage.CARD_CREATE_FAILED);
            }
            // 插入卡片和话题的关系
            for (Topic t: topics) {
                cardDao.insertCardTopic(UUID.getUUID(), card.getId(), t.getId());
            }
            txManager.commit(txStatus);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", ResponseMessage.CARD_CREATE_FAILED);
            txManager.rollback(txStatus);
            return response;
        }
        response.put("card", card);
        response.put("message", ResponseMessage.CARD_CREATE_SUCCESS);
        return response;
    }

    @Override
    public Map<String, Object> createShareCard(String text, String type, String shareId) {

        Map<String, Object> response = new HashMap<>(1);
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        // 创建卡片实例：id, text, type
        Card card = new Card(UUID.getUUID(), text, type);
        if (text == null) {
            text = "";
        }
        // 卡片内容长度限制
        if (text.length() > Card.TEXT_MAX_LEN) {
            response.put("message", ResponseMessage.CARD_TEXT_TOO_LONG);
            return response;
        }
        // 设置卡片内容
        card.setText(text);
        // 保证卡片创建者为当前登录用户
        card.setUser(user);
        card.setStatus(Card.EXIST);
        card.setTitle("转发卡片标题（不能显示）");

        // 卡片类型检测
        if (!card.getType().equals(Card.SHARE_CARD)) {
            response.put("message", ResponseMessage.CARD_TYPE_ERROR);
            return response;
        }

        // shareId不能为空
        if (StringUtils.isNullOrEmpty(shareId)) {
            response.put("message", ResponseMessage.SHARE_ID_IS_EMPTY);
            return response;
        }

        DefaultTransactionDefinition dtd = new DefaultTransactionDefinition();
        dtd.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus txStatus = txManager.getTransaction(dtd);
        try {
            int res = 0;

            // 设置分享
            cardDao.updateShareNumAddOne(shareId);
            card.setShare(cardDao.findOne(shareId, user.getId()));

            res += cardDao.insert(card);
            if (res <= 0) {
                throw new RuntimeException(ResponseMessage.CARD_CREATE_FAILED);
            }
            txManager.commit(txStatus);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", ResponseMessage.CARD_CREATE_FAILED);
            txManager.rollback(txStatus);
            return response;
        }
        response.put("card", card);
        response.put("message", ResponseMessage.CARD_CREATE_SUCCESS);
        return response;
    }

    @Override
    public Map<String, Object> createImageCard(String title, String text, String type, String[] topicNames,
            String[] remarks, MultipartFile[] files) {

        Map<String, Object> response = new HashMap<>(1);
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        // 创建卡片实例
        Card card = new Card(UUID.getUUID(), title, text, type);
        if (title == null) {
            title = "";
        }
        // 卡片标题长度限制
        if (title.length() > Card.TITLE_MAX_LEN) {
            response.put("message", ResponseMessage.CARD_TITLE_TOO_LONG);
            return response;
        }
        // 设置卡片标题
        card.setTitle(title);

        if (text == null) {
            text = "";
        }
        // 卡片内容长度限制
        if (text.length() > Card.TEXT_MAX_LEN) {
            response.put("message", ResponseMessage.CARD_TEXT_TOO_LONG);
            return response;
        }
        // 设置卡片内容
        card.setText(text);
        // 保证卡片创建者为当前登录用户
        card.setUser(user);

        // 卡片类型检测
        if (!card.getType().equals(Card.IMAGE_CARD)) {
            response.put("message", ResponseMessage.CARD_TYPE_ERROR);
            return response;
        }

        if (files == null) {
            files = new MultipartFile[0];
        }
        // 图片个数限制
        if (files.length > CardImage.MAX_NUM) {
            response.put("message", ResponseMessage.IMAGE_TOO_MUCH);
            return response;
        }

        // 图片备注名过少
        String[] fileNames = new String[files.length];
        if (files.length >= remarks.length) {
            for (int i = 0; i < files.length; i++) {
                if (i > remarks.length - 1) {
                    fileNames[i] = "image" + System.currentTimeMillis();
                }
                fileNames[i] = remarks[i];
            }
        }

        DefaultTransactionDefinition dtd = new DefaultTransactionDefinition();
        dtd.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus txStatus = txManager.getTransaction(dtd);
        try {
            int res = 0;

            List<CardImage> images = new ArrayList<>();
            for (int i=0; i< files.length; i++) {
                // 创建图片实例（卡片实例必须new），否则前端JSON解析数据会因为递归爆炸
                CardImage image = new CardImage(UUID.getUUID(), new Card(card.getId()));
                // 设置图片备注
                image.setRemark(fileNames[i]);

                // 图片路径不能为空
                String imgPath = files[i].getOriginalFilename();
                if (StringUtils.isNullOrEmpty(imgPath)) {
                    response.put("message", ResponseMessage.FILE_PATH_EMPTY);
                    return response;
                }
                String suffix = getFileSuffix(imgPath);
                // 图片类型限制
                if (!ConstantCollection.ALLOW_IMG_SUFFIX.contains(suffix)) {
                    response.put("message", ResponseMessage.FILE_TYPE_ERROR);
                    return response;
                }

                String imgName = System.currentTimeMillis() + "_cardImage_." + suffix;
                String key = uploadToQiniu(files[i], imgName);
                image.setUrl(qiniuDomain + key);

                // 图片列表添加图片对象
                images.add(image);
            }
            // 卡片添加图片列表
            card.setImages(images);

            // 插入话题
            List<Topic> topics = createTopics(topicNames, user);
            // 卡片添加话题列表
            card.setTopics(topics);

            // 插入卡片
            res += cardDao.insert(card);
            if (res <= 0) {
                throw new RuntimeException(ResponseMessage.CARD_CREATE_FAILED);
            }
            // 插入卡片和图片的关系
            imageDao.insertList(images);
            // 插入卡片和话题的关系
            for (Topic t: topics) {
                cardDao.insertCardTopic(UUID.getUUID(), card.getId(), t.getId());
            }
            txManager.commit(txStatus);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", ResponseMessage.CARD_CREATE_FAILED);
            txManager.rollback(txStatus);
            return response;
        }
        response.put("card", card);
        response.put("message", ResponseMessage.CARD_CREATE_SUCCESS);
        return response;
    }

    @Override
    public Map<String, Object> createVideoCard(String title, String text, String type, String[] topicNames,
            String fileName, MultipartFile file) {

        Map<String, Object> response = new HashMap<>(1);
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        // 创建卡片实例
        Card card = new Card(UUID.getUUID(), title, text, type);
        if (title == null) {
            title = "";
        }
        // 卡片标题长度限制
        if (title.length() > Card.TITLE_MAX_LEN) {
            response.put("message", ResponseMessage.CARD_TITLE_TOO_LONG);
            return response;
        }
        // 设置卡片标题
        card.setTitle(title);

        if (text == null) {
            text = "";
        }
        // 卡片内容长度限制
        if (text.length() > Card.TEXT_MAX_LEN) {
            response.put("message", ResponseMessage.CARD_TEXT_TOO_LONG);
            return response;
        }
        // 设置卡片内容
        card.setText(text);
        // 保证卡片创建者为当前登录用户
        card.setUser(user);
        card.setStatus(Card.EXIST);

        // 卡片类型检测
        if (!card.getType().equals(Card.VIDEO_CARD)) {
            response.put("message", ResponseMessage.CARD_TYPE_ERROR);
            return response;
        }

        DefaultTransactionDefinition dtd = new DefaultTransactionDefinition();
        dtd.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus txStatus = txManager.getTransaction(dtd);
        try {
            int res = 0;

            Video video = new Video(UUID.getUUID());
            String videoPath = file.getOriginalFilename();
            // 视频路径不为空
            if (StringUtils.isNullOrEmpty(videoPath)) {
                response.put("message", ResponseMessage.FILE_PATH_EMPTY);
                return response;
            }
            String suffix = getFileSuffix(videoPath);
            // 视频类型限制
            if (!(suffix.equals("mp4") || suffix.equals("avi") || suffix.equals("flv")) ) {
                response.put("message", ResponseMessage.FILE_TYPE_ERROR);
                return response;
            }

            // 设置卡片视频备注名
            video.setName(fileName);

            String videoName = System.currentTimeMillis() + "_cardVideo_." + suffix;
            String key = uploadToQiniu(file, videoName);
            video.setUrl(qiniuDomain +key);
            card.setVideo(video);
            videoDao.insert(video);

            // 插入话题
            List<Topic> topics = createTopics(topicNames, user);
            // 卡片添加话题列表
            card.setTopics(topics);

            res += cardDao.insert(card);
            if (res <= 0) {
                throw new RuntimeException(ResponseMessage.CARD_CREATE_FAILED);
            }
            // 插入卡片和话题的关系
            for (Topic t: topics) {
                cardDao.insertCardTopic(UUID.getUUID(), card.getId(), t.getId());
            }
            txManager.commit(txStatus);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", ResponseMessage.CARD_CREATE_FAILED);
            txManager.rollback(txStatus);
            return response;
        }
        response.put("card", card);
        response.put("message", ResponseMessage.CARD_CREATE_SUCCESS);
        return response;
    }

    @Override
    public Map<String, Object> createVoiceCard(String title, String text, String type, String[] topicNames,
            String fileName, MultipartFile file) {

        Map<String, Object> response = new HashMap<>(1);
        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        // 创建卡片实例
        Card card = new Card(UUID.getUUID(), title, text, type);
        if (title == null) {
            title = "";
        }
        // 卡片标题长度限制
        if (title.length() > Card.TITLE_MAX_LEN) {
            response.put("message", ResponseMessage.CARD_TITLE_TOO_LONG);
            return response;
        }
        // 设置卡片标题
        card.setTitle(title);

        if (text == null) {
            text = "";
        }
        // 卡片内容长度限制
        if (text.length() > Card.TEXT_MAX_LEN) {
            response.put("message", ResponseMessage.CARD_TEXT_TOO_LONG);
            return response;
        }
        // 设置卡片内容
        card.setText(text);
        // 保证卡片创建者为当前登录用户
        card.setUser(user);
        card.setStatus(Card.EXIST);

        // 卡片类型检测
        if (!card.getType().equals(Card.VOICE_CARD)) {
            response.put("message", ResponseMessage.CARD_TYPE_ERROR);
            return response;
        }

        DefaultTransactionDefinition dtd = new DefaultTransactionDefinition();
        dtd.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus txStatus = txManager.getTransaction(dtd);
        try {
            int res = 0;

            Voice voice = new Voice(UUID.getUUID());
            // 音频路径不能为空
            String voicePath = file.getOriginalFilename();
            if (StringUtils.isNullOrEmpty(voicePath)) {
                response.put("message", ResponseMessage.FILE_PATH_EMPTY);
                return response;
            }
            // 音频类型检测
            String suffix = getFileSuffix(voicePath);
            if (!suffix.equals("mp3")) {
                response.put("message", ResponseMessage.FILE_TYPE_ERROR);
                return response;
            }
            // 设置卡片音频备注名
            voice.setName(fileName);

            String voiceName = System.currentTimeMillis() + "_cardVoice_." + suffix;
            String key = uploadToQiniu(file, voiceName);
            voice.setUrl(qiniuDomain + key);
            card.setVoice(voice);
            voiceDao.insert(voice);

            // 插入话题
            List<Topic> topics = createTopics(topicNames, user);
            // 卡片添加话题列表
            card.setTopics(topics);

            res += cardDao.insert(card);
            if (res <= 0) {
                throw new RuntimeException(ResponseMessage.CARD_CREATE_FAILED);
            }
            // 插入卡片和话题的关系
            for (Topic t: topics) {
                cardDao.insertCardTopic(UUID.getUUID(), card.getId(), t.getId());
            }
            txManager.commit(txStatus);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", ResponseMessage.CARD_CREATE_FAILED);
            txManager.rollback(txStatus);
            return response;
        }
        response.put("card", card);
        response.put("message", ResponseMessage.CARD_CREATE_SUCCESS);
        return response;

    }

    @Override
    public Map<String, Object> delCard(String cardId) {
        Map<String, Object> response = new HashMap<>(1);

        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        // 卡片已经被删除
        if (isDeletedCard(cardId)) {
            response.put("message", ResponseMessage.CARD_IS_DELETED);
            return response;
        }

        // 用户为卡片所有者才可删除
        if (!isCardCreator(cardId, user.getId())) {
            response.put("message", ResponseMessage.NOT_CARD_CREATOR);
            return response;
        }

        // 删除云端图片文件
        List<CardImage> images = imageDao.findByCardId(cardId);
        for (CardImage image: images) {
            String[] tmp = image.getUrl().split("/");
            imgDeleteService.ImgDelete(tmp[tmp.length-1]);
        }

        DefaultTransactionDefinition dtd = new DefaultTransactionDefinition();
        dtd.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus txStatus = txManager.getTransaction(dtd);
        try {
            // 删除卡片和话题的关联
            cardDao.delCardTopic(cardId);
            // 删除卡片和图片的关联
            imageDao.delCardImage(cardId);
            int res = cardDao.delete(cardId);
            if (res != 1) {
                throw new RuntimeException(ResponseMessage.CARD_DEL_FAILED);
            }
            txManager.commit(txStatus);
        } catch (Exception e) {
            e.printStackTrace();
            txManager.rollback(txStatus);
            response.put("message", ResponseMessage.CARD_DEL_FAILED);
            return response;
        }
        response.put("message", ResponseMessage.CARD_DEL_SUCCESS);
        return response;
    }

    private String getFileSuffix(String filePath) {
        return filePath.substring(filePath.lastIndexOf(".")+1, filePath.length());
    }

    /**
     * 判断卡片是否已经是分享卡
     */
    private boolean isSharedCard(String cardId) {
        return cardDao.findByIdAndType(cardId, Card.SHARE_CARD) != null;
    }

    /**
     * 判断当前用户是否为卡片所有者
     */
    private boolean isCardCreator(String cardId, String userId) {
        return cardDao.findByIdAndUserId(cardId, userId) != null;
    }

    /**
     * 判断用户是否已经点赞过某卡片
     */
    private boolean isLikedCard(String cardId, String userId) {
        return cardDao.countLikeByIdAndUserId(cardId, userId) == 1;
    }

    /**
     * 卡片内容转换为摘要
     * 默认字数：Card.SUMMARY_WORAD_NUM
     * @param cards 卡片列表
     */
    private void textToSummary(List<Card> cards) {
        for (Card c: cards) {
            String text = c.getText();
            // 字数多于摘要字数时才进行转换
            if (text!=null && text.length() > Card.SUMMARY_WORAD_NUM) {
                c.setText(text.substring(0, Card.SUMMARY_WORAD_NUM - 1));
            }
            // 摘要转发原卡的字数
            if (c.getShare()!=null) {
                String shareText = c.getShare().getText();
                if (shareText != null && shareText.length() > Card.SUMMARY_WORAD_NUM) {
                    c.getShare().setText(shareText.substring(0, Card.SUMMARY_WORAD_NUM - 1));
                }
            }
        }
    }

    /**
     * 文件上传到七牛
     */
    private String uploadToQiniu(MultipartFile file, String fileName) throws IOException {
        String severFilePath = uploadPath + File.separator + fileName;
        File tempFile = new File(severFilePath);
        file.transferTo(tempFile);
        // 上传七牛云
        String key = imgUploadService.imageUpload(severFilePath, fileName);
        tempFile.delete();
        return key;
    }

    /**
     * 根据话题名处理话题
     * 话题名已存在则使用原有的；
     * 话题名不存在则根据新话题名创建；
     * 话题数量限制，仅添加前三个话题；
     */
    private List<Topic> createTopics(String[] topicName, User creator) {
        int topicNum = Topic.MAX_NUM;
        // 话题名为空
        if (topicName == null) {
            return new ArrayList<>();
        }
        if (topicName.length < Topic.MAX_NUM) {
            topicNum = topicName.length;
        }

        DefaultTransactionDefinition dtd = new DefaultTransactionDefinition();
        dtd.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        List<Topic> topics = new ArrayList<>();
        for (int i=0; i< topicNum; i++) {
            Topic topic = topicDao.findByFullName(topicName[i]);
            if (topic != null) {
                topics.add(topic);
            } else {
                TransactionStatus txStatus = txManager.getTransaction(dtd);
                try {
                    // 待添加话题不存在，则先插入
                    Topic newTopic = new Topic(UUID.getUUID(), topicName[i], creator);
                    int n = topicDao.insert(newTopic);
                    if (n == 1) {
                        topics.add(newTopic);
                    }
                    txManager.commit(txStatus);
                } catch (Exception e) {
                    e.printStackTrace();
                    txManager.rollback(txStatus);
                }

            }
        }
        return topics;
    }

    /**
     * 判断卡片是否已经被删除
     */
    private boolean isDeletedCard(String cardId) {
        return Card.DELETE.equals(cardDao.findStatusById(cardId));
    }

}
