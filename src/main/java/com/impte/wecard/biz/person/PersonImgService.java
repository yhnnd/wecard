package com.impte.wecard.biz.person;

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

public interface PersonImgService {
    Map<String, String> setAvatar(MultipartFile avatar) throws IOException;
    Map<String, String> setStyleImg(MultipartFile styleImg) throws IOException;
}
