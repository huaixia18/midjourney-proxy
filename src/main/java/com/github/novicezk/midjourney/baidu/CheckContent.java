package com.github.novicezk.midjourney.baidu;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
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

            String imgUrl = imagePath.replace("https://cdn.discordapp.com/", "https://ai-img-plus.caomaoweilai.com/") + "=&format=webp&quality=lossless&width=350&height=350";
//            String param = "imgUrl=" + imgUrl;

//            log.info("图片地址：" + imgUrl);

            URL url = new URL(imgUrl);

            // Read original image from url
            BufferedImage originalImage = ImageIO.read(url);

            // Compress the image
            BufferedImage compressedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            compressedImage.createGraphics().drawImage(originalImage, 0, 0, Color.WHITE, null);

            // Convert the compressed image to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(compressedImage, "png", baos);
            baos.flush();

            // Encode the byte array to Base64
            byte[] imageInByte = baos.toByteArray();
            String base64String = Base64.getEncoder().encodeToString(imageInByte);
            String param = "image=" + base64String;
            //调用图像审核接口
            String result = HttpUtil.post(BaiduSensitiveConfig.CHECK_IMAGE_URL, access_token, param);
            log.info("图片审核结果：" + result);
            //JSON解析对象
            return JSON.parseObject(result, ImageCheckReturn.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



}
