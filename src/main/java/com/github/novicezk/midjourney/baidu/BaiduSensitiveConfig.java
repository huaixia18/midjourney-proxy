package com.github.novicezk.midjourney.baidu;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 百度敏感信息配置
 */

@Data
@Component
@ConfigurationProperties(prefix = "baidu")
public class BaiduSensitiveConfig {

    /**
     * 百度云应用的AI：创建完应用后平台分配给此应用 AppID
     */
    private String APP_ID;

    /**
     * 百度云应用的AK：创建完应用后平台分配给此应用 API Key
     */
    private String API_KEY;

    /**
     * 百度云应用的SK：创建完应用后平台分配给此应用 Secret Key
     */
    private String SECRET_KEY;
    /**
     * 文本审核接口
     */
    public static final String CHECK_TEXT_URL = "https://aip.baidubce.com/rest/2.0/solution/v1/text_censor/v2/user_defined";

    /**
     * 图片审核接口
     */
    public static final String CHECK_IMAGE_URL = "https://aip.baidubce.com/rest/2.0/solution/v1/img_censor/v2/user_defined";

}

