package com.github.novicezk.midjourney.tencent;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tencent")
public class TencentConfig {

    /**
     * 腾讯云的SecretId
     */
    private String SECRET_ID;

    /**
     * 腾讯云的SecretKey
     */
    private String SECRET_KEY;

    /**
     * 腾讯云的region
     */
    private String REGION;

}
