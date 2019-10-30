package com.impte.wecard.biz.card.impl;

import com.impte.wecard.biz.card.CardSearchService;
import com.impte.wecard.dao.CardDao;
import com.impte.wecard.domain.po.Card;
import com.impte.wecard.domain.po.User;
import com.impte.wecard.utils.CurrentUtil;
import com.impte.wecard.utils.web.ResponseMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author justZero
 * @since 2018/3/12
 */
@Service
@AllArgsConstructor
public class CardSearchServiceImpl implements CardSearchService {

    private final CardDao cardDao;

    @Override
    public Map<String, Object> search(String key, String value,
                                      int start, int limit, String sortKey) {
        Map<String, Object> response = new HashMap<>(3);

        List<Card> cards;
        try {
            User user = CurrentUtil.getUser();
            switch (key) {
                case "topic":
                    cards = cardDao.findByTopicName(value, start, limit, sortKey,
                        user == null ? null : user.getId());
                    break;
                case "content":
                    cards = cardDao.findByContent(value, start, limit, sortKey,
                        user == null ? null : user.getId());
                    break;
                default:
                    response.put("message", ResponseMessage.CARD_SEARCH_KEY_ERROR);
                    return response;
            }

            if (cards.size() == 0) {
                response.put("message", ResponseMessage.NO_DATA);
                return response;
            }
            response.put("cards", cards);
            response.put("keyword", value);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", ResponseMessage.CARD_SEARCH_FAILED);
            return response;
        }

        response.put("message", ResponseMessage.CARD_SEARCH_SUCCESS);
        return response;
    }

}
