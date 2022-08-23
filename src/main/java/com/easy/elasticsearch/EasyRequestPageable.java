package com.easy.elasticsearch;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@Builder
public class EasyRequestPageable {
    @Min(value = 1,message = "page start range greater than 1")
    private int page;
    @Min(value = 1,message = "size range 1-100 ")
    @Max(value = 100 ,message = "size range 1-100 ")
    private int size;
    private EasySort sort;
}
