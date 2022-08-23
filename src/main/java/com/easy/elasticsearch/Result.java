package com.easy.elasticsearch;

import lombok.Data;

import java.util.List;

@Data
public class Result<T> {
    private List<T> data;
    private int totalCount;
    private int pageCount;

    public Result(){}

    public Result(List<T> data , int totalCount , int pageCount){
        this.data = data;
        this.totalCount = totalCount ;
        this.pageCount = pageCount ;
    }
}
