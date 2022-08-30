package com.easy.entity;

import lombok.Data;

@Data
public class EasySort {
    private String sortField;
    private EasySortEnum order;

    public EasySort(String sortField , EasySortEnum order){
        this.sortField = sortField ;
        this.order = order ;
    }
}
