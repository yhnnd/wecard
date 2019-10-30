package com.impte.wecard.biz.begin.impl;

import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.impte.wecard.biz.begin.ShortMsgService;
import org.springframework.stereotype.Service;

@Service
public class ShortMsgServiceImpl implements ShortMsgService {

    private final int appid = 1400068285;
    private final String appkey = "a81011e26c292ab552b63a6e2f86740d";

    @Override
    public String sendMessage(String phoneNumber, String message) {

        String result;
        //发送短信
        SmsSingleSenderResult res = null;
        try {
            SmsSingleSender ssender = new SmsSingleSender(appid, appkey);
            res = ssender.send(
                    0,
                    "86",
                    phoneNumber,
                    message,
                    "",
                    ""
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (res.result == 0){
            result = "Send success";
        }else if (res.result == 1023 || res.result == 1024 || res.result == 1025){
            result = "Frequency limit";
        }else {
            result = "Send fail";
        }

        return result;
    }
}
