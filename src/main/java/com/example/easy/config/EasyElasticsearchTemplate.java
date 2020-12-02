package com.example.easy.config;

import org.elasticsearch.client.Client;
import org.springframework.context.annotation.Configuration;

@EasyElasticsearchClient
@Configuration
public interface EasyElasticsearchTemplate extends Client {
}
