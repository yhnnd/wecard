package com.impte.wecard.biz.card.impl;

import com.impte.wecard.biz.card.CommentService;
import com.impte.wecard.dao.CommentDao;
import com.impte.wecard.domain.po.Comment;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import com.impte.wecard.utils.UUID;
import com.impte.wecard.utils.web.ResponseMessage;
import com.mysql.jdbc.StringUtils;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * @author justZero
 * @since 2018/3/9
 */
@Service
@AllArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentDao commentDao;

    private final DataSourceTransactionManager txManager;

    @Override
    public Map<String, Object> getCmntsByCardId(
            String cardId, Integer start, Integer limit, String sortKey) {
        Map<String, Object> response = new HashMap<>(2);
        try {
            List<Comment> comments = commentDao.findByCardId(cardId, start, limit, sortKey);
            if (comments.size() == 0) {
                response.put("message", ResponseMessage.NO_DATA);
                return response;
            }
            response.put("comments", comments);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", ResponseMessage.COMMENT_LOAD_FAILED);
            return response;
        }
        response.put("message", ResponseMessage.COMMENT_LOAD_SUCCESS);
        return response;
    }

    @Override
    public Map<String, Object> getLv1Cmnts(
            String cardId, Integer start, Integer limit, String sortKey) {
        Map<String, Object> response = new HashMap<>(2);
        try {
            List<Comment> comments = commentDao.findLv1ByCardId(cardId, start, limit, sortKey);
            if (comments.size() == 0) {
                response.put("message", ResponseMessage.NO_DATA);
                return response;
            }
            response.put("comments", comments);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", ResponseMessage.COMMENT_LOAD_FAILED);
            return response;
        }
        response.put("message", ResponseMessage.COMMENT_LOAD_SUCCESS);
        return response;
    }

    @Override
    public Map<String, Object> getLv2Cmnts(
            String commentId, Integer start, Integer limit) {
        Map<String, Object> response = new HashMap<>(2);
        try {
            List<Comment> comments = commentDao.findLv2(commentId, start, limit);
            if (comments.size() == 0) {
                response.put("message", ResponseMessage.NO_DATA);
                return response;
            }
            response.put("comments", comments);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", ResponseMessage.COMMENT_LOAD_FAILED);
            return response;
        }
        response.put("message", ResponseMessage.COMMENT_LOAD_SUCCESS);
        return response;
    }

    @Override
    public Map<String, Object> userLikeCmnt(String commentId) {
        Map<String, Object> response = new HashMap<>(2);

        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        // 评论ID不能为空
        if (StringUtils.isNullOrEmpty(commentId)) {
            response.put("message", ResponseMessage.COMMENT_ID_IS_EMPTY);
            return response;
        }

        // 不能重复点赞
        if (isLikeCmnt(commentId, user.getId())) {
            response.put("message", ResponseMessage.LIKE_COMMENT_REPEATED);
            return response;
        }

        DefaultTransactionDefinition dtd = new DefaultTransactionDefinition();
        dtd.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus txStatus = txManager.getTransaction(dtd);
        try {
            int res = commentDao.insertLikeComment(
                    UUID.getUUID(), commentId, user.getId());
            if (res != 1) {
                throw new RuntimeException(ResponseMessage.LIKE_COMMENT_FAILED);
            }
            txManager.commit(txStatus);
        } catch (Exception e) {
            e.printStackTrace();
            txManager.rollback(txStatus);
            response.put("message", ResponseMessage.LIKE_COMMENT_FAILED);
            return response;
        }
        response.put("message", ResponseMessage.LIKE_COMMENT_SUCCESS);
        return response;
    }

    @Override
    public Map<String, Object> userUnlikeCmnt(String commentId) {
        Map<String, Object> response = new HashMap<>(2);

        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        // 评论ID不能为空
        if (StringUtils.isNullOrEmpty(commentId)) {
            response.put("message", ResponseMessage.COMMENT_ID_IS_EMPTY);
            return response;
        }

        // 只能取消自己的点赞
        if (!isLikeCmnt(commentId, user.getId())) {
            response.put("message", ResponseMessage.DID_NOT_LIKE_COMMENT);
            return response;
        }

        DefaultTransactionDefinition dtd = new DefaultTransactionDefinition();
        dtd.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus txStatus = txManager.getTransaction(dtd);
        try {
            int res = commentDao.deleteLikeComment(commentId, user.getId());
            if (res != 1) {
                throw new RuntimeException(ResponseMessage.UNLIKE_COMMENT_FAILED);
            }
            txManager.commit(txStatus);
        } catch (Exception e) {
            e.printStackTrace();
            txManager.rollback(txStatus);
            response.put("message", ResponseMessage.UNLIKE_COMMENT_FAILED);
            return response;
        }
        response.put("message", ResponseMessage.UNLIKE_COMMENT_SUCCESS);
        return response;
    }

    @Override
    public Map<String, Object> createCmnt(Comment comment) {
        Map<String, Object> response = new HashMap<>(1);

        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        // 设置用户
        comment.setUser(new User(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatarUrl()
        ));

        // 检测评论的合法性
        String cmntText = comment.getText();
        if (cmntText.length() > Comment.TEXT_MAX_LEN) {
            response.put("message", ResponseMessage.COMMENT_TEXT_TOO_LONG);
            return response;
        }

        DefaultTransactionDefinition dtd = new DefaultTransactionDefinition();
        dtd.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus txStatus = txManager.getTransaction(dtd);
        try {
            // 设置评论ID
            comment.setId(UUID.getUUID());
            // 设置创建时间
            comment.setCreatedTime(new Timestamp(System.currentTimeMillis()));
            int res = commentDao.insert(comment);
            if (res != 1) {
                throw new RuntimeException(ResponseMessage.COMMENT_CREATE_FAILED);
            }
            txManager.commit(txStatus);
        } catch (Exception e) {
            e.printStackTrace();
            txManager.rollback(txStatus);
            response.put("message", ResponseMessage.COMMENT_CREATE_FAILED);
            return response;
        }
        // 设置状态
        comment.setStatus(Comment.EXIST);
        response.put("comment", comment);
        response.put("message", ResponseMessage.COMMENT_CREATE_SUCCESS);
        return response;
    }

    @Override
    public Map<String, Object> delCmnt(String commentId) {
        Map<String, Object> response = new HashMap<>(1);

        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        // 检测评论是否已经被删除
        if (isDeletedCmnt(commentId)) {
            response.put("message", ResponseMessage.COMMENT_IS_DELETED);
            return response;
        }

        // 检测是否为评论所有者
        if (!isCmntCreator(commentId, user.getId())) {
            response.put("message", ResponseMessage.NOT_COMMENT_CREATOR);
            return response;
        }

        DefaultTransactionDefinition dtd = new DefaultTransactionDefinition();
        dtd.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus txStatus = txManager.getTransaction(dtd);
        try {
            // 删除该评论
            int res = commentDao.delete(commentId);
            if (res != 1) {
                throw new RuntimeException(ResponseMessage.COMMENT_DEL_FAILED);
            }
            txManager.commit(txStatus);
        } catch (Exception e) {
            e.printStackTrace();
            txManager.rollback(txStatus);
            response.put("message", ResponseMessage.COMMENT_DEL_FAILED);
            return response;
        }
        response.put("message", ResponseMessage.COMMENT_DEL_SUCCESS);
        return response;
    }

    @Override
    public Map<String, Object> getUserCmnt(int start, int limit) {
        Map<String, Object> response = new HashMap<>(2);

        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        try {
            List<Comment> comments = commentDao.findByUserId(user.getId(), start, limit);
            if (comments.size() == 0) {
                response.put("message", ResponseMessage.NO_DATA);
                return response;
            }
            response.put("comments", comments);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", ResponseMessage.COMMENT_LOAD_FAILED);
            return response;
        }

        response.put("message", ResponseMessage.COMMENT_LOAD_SUCCESS);
        return response;
    }

    @Override
    public Map<String, Object> getCmntToUser(int start, int limit) {
        Map<String, Object> response = new HashMap<>(2);

        User user = CurrentUtil.getUser();

        // 已经过登录状态检查拦截器鉴权

        try {
            List<Comment> comments = commentDao.findToUserByUserId(user.getId(), start, limit);
            if (comments.size() == 0) {
                response.put("message", ResponseMessage.NO_DATA);
                return response;
            }
            response.put("comments", comments);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", ResponseMessage.COMMENT_LOAD_FAILED);
            return response;
        }

        response.put("message", ResponseMessage.COMMENT_LOAD_SUCCESS);
        return response;
    }


    /**
     * 判断是否为评论所有者
     */
    private boolean isCmntCreator(String commentId, String userId) {
        return commentDao.findByIdAndUserId(commentId, userId) != null;
    }

    /**
     * 判断用户是否已经点赞过某评论
     */
    private boolean isLikeCmnt(String commentId, String userId) {
        return commentDao.countLikeByIdAndUserId(commentId, userId) == 1;
    }

    /**
     * 判断评论是否已经被删除
     */
    private boolean isDeletedCmnt(String commentId) {
        return Comment.DELETE.equals(commentDao.findStatusById(commentId));
    }

}
