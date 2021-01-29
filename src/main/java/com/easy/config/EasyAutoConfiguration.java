package com.easy.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EasyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EasyElasticsearchService easyElasticsearchTemplate(){
        return new EasyElasticsearchService();
    }
}
