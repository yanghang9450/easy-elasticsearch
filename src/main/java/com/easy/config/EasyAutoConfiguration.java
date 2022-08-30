package com.easy.config;

import com.easy.elasticsearch.EasyElasticsearchTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties({ConfigProperties.class})
@Import({EasyElasticsearchConfiguration.class})
public class EasyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EasyElasticsearchTemplate easyElasticsearchTemplate(){
        return new EasyElasticsearchTemplate();
    }
}
