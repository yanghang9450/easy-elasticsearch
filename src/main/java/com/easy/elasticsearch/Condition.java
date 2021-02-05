package com.easy.elasticsearch;

import lombok.Data;

@Data
public class Condition {
    private String match;
    private String key;
    private String value;
}
