package com.easy.response;

import lombok.Data;

import java.util.List;
/**
 * @author yanghang
 */
@Data
public class ListResult<T> {
    private List<T> data;
    private int totalCount;
    private int pageCount;
    public ListResult(){}
    public ListResult(List<T> data , int totalCount , int pageCount){
        this.data = data;
        this.totalCount = totalCount ;
        this.pageCount = pageCount ;
    }
}
