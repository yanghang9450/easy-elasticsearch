package com.easy.config;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.util.StringUtils;

import java.util.Objects;
/**
 * @author yanghang
 */
@Slf4j
@ConditionalOnProperty(
        name = {"easy.elasticsearch.cluster.enabled"},
        havingValue = "true"
)
public class EasyElasticsearchConfiguration {
    private static final long default_timeout_millis = 60000;
    @Bean
    public RestHighLevelClient restHighLevelClient(ConfigProperties configProperties){
        String[] urls = configProperties.getUrls().split(",");
        ClientConfiguration configuration =
                StringUtils.hasText(configProperties.getUsername()) && StringUtils.hasText(configProperties.getPassword()) ?
                        ClientConfiguration.builder()
                                .connectedTo(urls)
                                .withBasicAuth(configProperties.getUsername(), configProperties.getPassword())
                                .withConnectTimeout(
                                        Objects.isNull(configProperties.getConnectTimeout()) ?
                                                default_timeout_millis : configProperties.getConnectTimeout()
                                ).withSocketTimeout(
                                        Objects.isNull(configProperties.getSocketTimeout()) ?
                                                default_timeout_millis : configProperties.getSocketTimeout()
                                ).build()
                        : ClientConfiguration.builder()
                                .connectedTo(urls)
                                .withConnectTimeout(
                                        Objects.isNull(configProperties.getConnectTimeout()) ?
                                                default_timeout_millis : configProperties.getConnectTimeout()
                                ).withSocketTimeout(
                                        Objects.isNull(configProperties.getSocketTimeout()) ?
                                                default_timeout_millis : configProperties.getSocketTimeout()
                                ).build();

        return RestClients.create(configuration).rest();
    }
}
