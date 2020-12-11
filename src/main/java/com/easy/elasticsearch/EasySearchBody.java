package com.easy.elasticsearch;

import lombok.Data;

import java.util.List;

@Data
public class EasySearchBody {

    /**
     * elasticsearch index
     */
    private String index;
    /**
     * elasticsearch index type
     */
    private String type;

    /**
     * search target field
     */
    private List<String> searchTargetField;

    /**
     * search value
     */
    private String searchValue;

    /**
     * search sort
     */
    private EasySort sort;

    /**
     * conditions
     */
    private List<Condition> conditions;

    /**
     * page
     */
    private int page;

    /**
     * size
     */
    private int size;

}
