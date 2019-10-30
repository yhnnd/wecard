package com.impte.wecard.biz.find;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public interface FindRequestService {
    Map<String, Object> findRequest(String requestId);
}
