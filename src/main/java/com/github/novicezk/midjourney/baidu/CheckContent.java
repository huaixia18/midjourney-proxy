package com.github.novicezk.midjourney.baidu;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.util.Map;
import java.util.TreeMap;

/**
 * 内容审核
 */
@Component
public class CheckContent {


    @Autowired
    private BaiduAccessToken baiduAccessToken;

    /**
     * 文本审核
     *
     * @param text 需要审核的文本
     * @return
     */
    public TextCheckReturn checkText(String text) {
        Map<String, Object> map = new TreeMap<String, Object>();
        //获取access_token
        String access_token = baiduAccessToken.getAuth();
        try {
            //设置请求的编码
            String param = "text=" + URLEncoder.encode(text, "UTF-8");

            //调用文本审核接口并取得结果
            String result = HttpUtil.post(BaiduSensitiveConfig.CHECK_TEXT_URL, access_token, param);
            // JSON解析对象
            TextCheckReturn tcr = JSON.parseObject(result, TextCheckReturn.class);
            return tcr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
