package com.impte.wecard.biz.chat;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

public interface SendImgService {
    String sendImgToChatItem(String chatItemId, MultipartFile image, String fileType) throws IOException;
}
