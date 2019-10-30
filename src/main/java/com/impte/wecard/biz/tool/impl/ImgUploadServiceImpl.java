package com.impte.wecard.biz.tool.impl;

import com.google.gson.Gson;
import com.impte.wecard.biz.tool.ImgUploadService;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ImgUploadServiceImpl implements ImgUploadService {

    @Value("${qiniu.ak}")
    private String accessKey;

    @Value("${qiniu.sk}")
    private String secretKey;

    @Value("${qiniu.bucket}")
    private String bucket;

    @Override
    public String imageUpload(String imgPath, String imgName) {

        Configuration cfg = new Configuration(Zone.zone1());
        UploadManager uploadManager = new UploadManager(cfg);

        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);

        DefaultPutRet putRet = null;
        try {
            Response response = uploadManager.put(imgPath, imgName, upToken);
            //解析上传成功的结果
            putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
        } catch (QiniuException ex) {
            ex.printStackTrace();
        }

        if (putRet != null){
            return putRet.key;
        }else {
            return null;
        }
    }
}
