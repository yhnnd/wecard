package com.impte.wecard.biz.tool.impl;

import com.impte.wecard.biz.tool.ImgDeleteService;
import com.qiniu.util.Auth;
import com.qiniu.util.UrlSafeBase64;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ImgDeleteServiceImpl implements ImgDeleteService {

    @Value("${qiniu.ak}")
    private String accessKey;

    @Value("${qiniu.sk}")
    private String secretKey;

    @Value("${qiniu.bucket}")
    private String bucket;

    @Override
    public void ImgDelete(String imageName) {
        //获取Auth对象
        Auth auth = Auth.create(accessKey, secretKey);
        //指定需要删除的空间和文件，格式为： <bucket>:<key>
        String entry = bucket + ":" + imageName;
        //通过安全base64编码方式进行编码处理
        String encodedEntryURI = UrlSafeBase64.encodeToString(entry);
        //指定接口
        String target = "/delete/" + encodedEntryURI + "\n";
        //获取token，即操作凭证
        String access_token = auth.sign(target);
        //指定好请求的delete接口地址
        String url = "http://rs.qiniu.com/delete/" + encodedEntryURI;

        //通过Okhttp jar 包封装的对象 发起网络请求
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Authorization", "QBox " + access_token).build();
        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        Auth auth = Auth.create(AccountMgr.ACCESS_KEY, AccountMgr.SECRET_KEY);
//        Configuration config = new Configuration(Zone.autoZone());
//        BucketManager bucketMgr = new BucketManager(auth, config);
//
//        //指定需要删除的文件，和文件所在的存储空间
//        String bucketName = "java-bucket";
//        String key = "blob_11_9_01.png";
//        String key2 = "blob_11_9_02.png";
//        try {
//            bucketMgr.delete(bucketName, key);//当前为7.2.1；  7.2.2后才能传多个key ，即：第二个参数为数组 (String... deleteTargets)
//        } catch (QiniuException e) {
//            e.printStackTrace();
//        }
    }
}
