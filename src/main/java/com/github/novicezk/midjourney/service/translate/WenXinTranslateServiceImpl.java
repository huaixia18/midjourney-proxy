package com.github.novicezk.midjourney.service.translate;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.baidu.CacheManager;
import com.github.novicezk.midjourney.dto.wenxin.QianFanChatDto;
import com.github.novicezk.midjourney.dto.wenxin.QianFanMessageDto;
import com.github.novicezk.midjourney.dto.wenxin.QianFanTokenDto;
import com.github.novicezk.midjourney.service.TranslateService;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author biliyu
 * @date 2023/12/10 23:06
 */
public class WenXinTranslateServiceImpl implements TranslateService {

    private final String apiKey;
    private final String secretKey;

    private static final String QIANFAN_TOKEN_API = "https://aip.baidubce.com/oauth/2.0/token";

    private static final String QIANFAN_TRANSLATE_API = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions_pro";

    public WenXinTranslateServiceImpl(ProxyProperties.WenxinTranslateConfig translateConfig) {
        this.apiKey = translateConfig.getApiKey();
        this.secretKey = translateConfig.getSecretKey();
        if (!CharSequenceUtil.isAllNotBlank(this.apiKey, this.secretKey)) {
            throw new BeanDefinitionValidationException("wenxin.api-key或wenxin.secret-key未配置");
        }
    }


    @Override
    public String translateToEnglish(String prompt) {
        QianFanMessageDto m1 = new QianFanMessageDto();
        m1.setRole("user");
        m1.setContent("请讲我输入的内容翻译成英文，我输入的内容如下：" + prompt);
        Map<String, List<QianFanMessageDto>> param = new HashMap<>();
        param.put("messages", List.of(m1));
        String json = JSON.toJSONString(param);
        String url = QIANFAN_TRANSLATE_API + "?access_token=" + getToken();
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), json);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = null;
        try {
            //将请求添加到请求队列等待执行，并返回执行后的Response对象
            response = client.newCall(request).execute();
            //获取Http Status Code.其中200表示成功
            if (response.code() == 200) {
                //这里需要注意，response.body().string()是获取返回的结果，此句话只能调用一次，再次调用获得不到结果。
                //所以先将结果使用result变量接收
                String result = response.body().string();
                QianFanChatDto qianFanChatDto = JSONObject.parseObject(result, QianFanChatDto.class);
                return qianFanChatDto.getResult();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                response.body().close();
            }
        }
        return "";
    }

    public String getToken() {
        Object qianfanToken = CacheManager.get("qianfan_token");
        if (qianfanToken != null) {
            return qianfanToken.toString();
        }
        //百度云应用的AK  百度云应用的SK
        return getToken(apiKey, secretKey);
    }

    public String getToken(String ak, String sk) {

        Object qianfanToken = CacheManager.get("qianfan_token");
        if (qianfanToken != null) {
            return qianfanToken.toString();
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request
                .Builder() //利用建造者模式创建Request对象
                .url(QIANFAN_TOKEN_API + "?grant_type=client_credentials" + "&client_id=" + ak + "&client_secret=" + sk) //设置请求的URL
                .build(); //生成Request对象
        Response response = null;
        try {
            //将请求添加到请求队列等待执行，并返回执行后的Response对象
            response = client.newCall(request).execute();
            //获取Http Status Code.其中200表示成功
            if (response.code() == 200) {
                //这里需要注意，response.body().string()是获取返回的结果，此句话只能调用一次，再次调用获得不到结果。
                //所以先将结果使用result变量接收
                String result = response.body().string();
                QianFanTokenDto qianFanTokenDto = JSONObject.parseObject(result, QianFanTokenDto.class);
                CacheManager.put("qianfan_token", qianFanTokenDto.getAccess_token(), 30, TimeUnit.DAYS);
                return qianFanTokenDto.getAccess_token();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                response.body().close();
            }
        }
        return "";
    }

}
