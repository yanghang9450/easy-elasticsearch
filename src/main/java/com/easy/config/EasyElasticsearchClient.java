package com.easy.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({EasyElasticsearchConfiguration.class})
public @interface EasyElasticsearchClient {
}
