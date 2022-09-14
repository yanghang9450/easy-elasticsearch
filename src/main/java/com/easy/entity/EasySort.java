package com.easy.entity;

import lombok.Data;
/**
 * @author yanghang
 */
@Data
public class EasySort {
    private String sortField;
    private EasySortEnum order;

    public EasySort(String sortField , EasySortEnum order){
        this.sortField = sortField ;
        this.order = order ;
    }
}
