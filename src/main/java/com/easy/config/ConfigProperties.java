package com.easy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
        prefix = "easy.elasticsearch.cluster"
)
@Data
public class ConfigProperties {
    private String name;
    private String nodes;
    private Integer port;
    private String username;
    private String password;

    public ConfigProperties(){}
}
