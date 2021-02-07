package com.easy.elasticsearch;

import lombok.Data;

@Data
public class EasySort {
    private String sortField;
    private EasySortEnum order;
}
