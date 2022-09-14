package com.easy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
/**
 * @author yanghang
 */
@ConfigurationProperties(
        prefix = "easy.elasticsearch.cluster"
)
@Data
public class ConfigProperties {
    private String urls;
    private String username;
    private String password;
    private Long socketTimeout;
    private Long connectTimeout;

    public ConfigProperties(){}
}
