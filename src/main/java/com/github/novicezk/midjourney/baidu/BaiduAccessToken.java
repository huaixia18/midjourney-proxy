package com.github.novicezk.midjourney.baidu;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSONObject;
import com.github.novicezk.midjourney.ProxyProperties;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.stereotype.Component;

@Component
public class BaiduAccessToken {

    private final String appid;

    private final String appKey;
    private final String appSecret;


    public BaiduAccessToken(BaiduSensitiveConfig baiduSensitiveConfig) {
        this.appid = baiduSensitiveConfig.getAPP_ID();
        this.appKey = baiduSensitiveConfig.getAPI_KEY();
        this.appSecret = baiduSensitiveConfig.getSECRET_KEY();
        if (!CharSequenceUtil.isAllNotBlank(this.appid, this.appSecret)) {
            throw new BeanDefinitionValidationException("baidu.appid或baidu.app-secret或baidu.app-key未配置");
        }
    }

    /**
     * 获取API访问权限token
     *
     * @return
     */
    public String getAuth() {
        Object baiduToken = CacheManager.get("baidu_token");
        if (baiduToken != null) {
            return baiduToken.toString();
        }
        //百度云应用的AK  百度云应用的SK
        return getAuth(appKey, appSecret);
    }

    /**
     * 获取API访问权限token，该token有一定的有效期，需要自行管理，当失效时需重新获取。
     *
     * @param ak 百度云官网获取的 API Key
     * @param sk 百度云官网获取的 Securet Key
     * @return assess_token 示例：
     * "24.460da4889caad24cccdb1fea17221975.2592000.1491995545.282335-1234567"
     */
    public String getAuth(String ak, String sk) {
        // 获取token地址
        String authHost = "https://aip.baidubce.com/oauth/2.0/token?";
        String getAccessTokenUrl = authHost
                // 1. grant_type为固定参数
                + "grant_type=client_credentials"
                // 2. 官网获取的 API Key
                + "&client_id=" + ak
                // 3. 官网获取的 Secret Key
                + "&client_secret=" + sk;
        try {
            URL realUrl = new URL(getAccessTokenUrl);
            // 打开和URL之间的连接
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
//            for (String key : map.keySet()) {
//                System.err.println(key + "--->" + map.get(key));
//            }
            // 定义 BufferedReader输入流来读取URL的响应
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String result = "";
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            /**
             * 返回结果示例
             */
            JSONObject jsonObject = JSONObject.parseObject(result);
            String access_token = jsonObject.getString("access_token");
            CacheManager.put("baidu_token", access_token, 30, TimeUnit.DAYS);
            return access_token;
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return null;
    }

}
