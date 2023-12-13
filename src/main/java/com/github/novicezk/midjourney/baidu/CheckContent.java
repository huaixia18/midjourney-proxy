package com.github.novicezk.midjourney.baidu;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URLEncoder;
import java.util.Map;
import java.util.TreeMap;

/**
 * 内容审核
 */
@Slf4j
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
            log.info("文本审核结果：" + result);
            // JSON解析对象
            TextCheckReturn tcr = JSON.parseObject(result, TextCheckReturn.class);
            return tcr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 图像审核
     *
     * @param imagePath 需要审核的图片路径
     * @return
     */
    public ImageCheckReturn checkImage(String imagePath) {
        //获取access_token
        String access_token = baiduAccessToken.getAuth();
        try {

            String param = "imgUrl=" + imagePath.replace("https://cdn.discordapp.com/", "https://ai-img-plus.caomaoweilai.com/") + "=&format=webp&quality=lossless&width=350&height=350";

            //调用图像审核接口

            String result = HttpUtil.post(BaiduSensitiveConfig.CHECK_IMAGE_URL, access_token, param);
            log.info("图片审核结果：" + result);
            //JSON解析对象
            ImageCheckReturn icr = JSON.parseObject(result, ImageCheckReturn.class);
            return icr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



}
