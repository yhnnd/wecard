package com.impte.wecard.utils.web;

/**
 * @author justZero
 * @since  2018/3/6
 */
public interface ResponseMessage {

    String ERROR    = "error";
    String SUCCESS  = "success";

    /**
     * 用户相关
     */
    String NOT_LOGIN  = "Please login";

    String NO_DATA    = "no data";

    /**
     * 卡片相关
     */
    String CARD_LOAD_SUCCESS        = "card load success";
    String CARD_LOAD_FAILED         = "card load failed";
    String LIKE_CARD_SUCCESS        = "like card success";
    String LIKE_CARD_FAILED         = "like card failed";
    String UNLIKE_CARD_SUCCESS      = "unlike card success";
    String UNLIKE_CARD_FAILED       = "unlike card failed";
    String CARD_SEARCH_SUCCESS      = "card search success";
    String CARD_SEARCH_FAILED       = "card search failed";
    String CARD_SEARCH_KEY_ERROR    = "card search key error";
    String CARD_CREATE_SUCCESS      = "card create success";
    String CARD_CREATE_FAILED       = "card create failed";
    String CARD_TYPE_ERROR          = "card type error";
    String CARD_DEL_SUCCESS         = "card del success";
    String CARD_DEL_FAILED          = "card del failed";
    String NOT_CARD_CREATOR         = "not card creator";
    String IMAGE_TOO_MUCH           = "image too much";
    String CARD_TEXT_TOO_LONG       = "card text too long";
    String CARD_TITLE_TOO_LONG      = "card title too long";
    String LIKE_CARD_REPEATED       = "like card repeated";
    String DID_NOT_LIKE_CARD        = "did not like card";
    String SHARE_ID_IS_EMPTY        = "share id is empty";
    String CARD_IS_DELETED          = "card has been deleted";
    String CARD_ID_IS_EMPTY         = "card id is empty";
    String IS_NOT_SHARED_CARD       = "is not shared card";

    /**
     * 评论相关
     */
    String COMMENT_LOAD_SUCCESS     = "comment load success";
    String COMMENT_LOAD_FAILED      = "comment load failed";
    String LIKE_COMMENT_SUCCESS     = "like comment success";
    String LIKE_COMMENT_FAILED      = "like comment failed";
    String UNLIKE_COMMENT_SUCCESS   = "unlike comment success";
    String UNLIKE_COMMENT_FAILED    = "unlike comment failed";
    String COMMENT_CREATE_SUCCESS   = "comment create success";
    String COMMENT_CREATE_FAILED    = "comment create failed";
    String COMMENT_DEL_SUCCESS      = "comment del success";
    String COMMENT_DEL_FAILED       = "comment del failed";
    String NOT_COMMENT_CREATOR      = "not comment creator";
    String COMMENT_TEXT_TOO_LONG    = "comment text too long";
    String LIKE_COMMENT_REPEATED    = "like comment repeated";
    String DID_NOT_LIKE_COMMENT     = "did not like comment";
    String COMMENT_IS_DELETED       = "comment has been deleted";
    String COMMENT_ID_IS_EMPTY      = "comment id is empty";

    /**
     * 话题相关
     */
    String TOPIC_CREATE_FAILED      = "topic create failed";
    String TOPIC_CREATE_SUCCESS     = "topic create success";
    String NOT_TOPIC_CREATOR        = "not topic creator";
    String TOPIC_IS_USING           = "topic is using";
    String TOPIC_DELETE_SUCCESS     = "topic delete success";
    String TOPIC_DELETE_FAILED      = "topic delete failed";
    String TOPIC_LOAD_SUCCESS       = "topic load success";
    String TOPIC_LOAD_FAILED        = "topic load failed";
    String TOPIC_NAME_TOO_LONG      = "topic name too long";
    String TOPIC_NAME_ILLEGAL       = "topic name illegal";

    /**
     * 文件相关
     */
    String FILE_PATH_EMPTY          = "file path empty";
    String FILE_TYPE_ERROR          = "file type error";
}
