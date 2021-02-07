package com.easy.elasticsearch;

import lombok.Data;

import java.util.List;

@Data
public class EasySearchBody {
    private String index;
    private String type;
    private List<String> searchTargetField;
    private String searchValue;
    private EasySort sort;
    private List<Condition> conditions;
    private int page;
    private int size;
    private boolean openIK;
    private boolean openPinYin;
}
