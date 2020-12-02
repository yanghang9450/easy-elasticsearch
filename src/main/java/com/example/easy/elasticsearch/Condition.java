package com.example.easy.elasticsearch;

import lombok.Data;

import java.util.Map;

@Data
public class Condition {

    private String match;
    /**
     * 参数 ker
     */
    private String key;

    private String value;
}
