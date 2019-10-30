package com.impte.wecard.biz.card;

import java.util.Map;

/**
 * 卡片搜索相关服务接口
 * @author justZero
 * @since 2018/3/12
 */
public interface CardSearchService {

    Map<String, Object> search(String key, String value,
        int start, int limit,
        String sortKey);
}
