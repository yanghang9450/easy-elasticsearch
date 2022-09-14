package com.easy.entity;
/**
 * @author yanghang
 */
public enum Match {
    /**
     * 精确匹配查询短语，需要全部单词和顺序要完全一样，标点符号除外
     */
    MATCH_PHRASE,
    /**
     * 与 match_phrase 用法是一样的，区别就在于它允许对最后一个词条前缀匹配
     */
    MATCH_PHRASE_PREFIX,
    /**
     * 输入的查询内容是什么，就会按照什么去查询，并不会解析查询内容，对它分词
     */
    TERM,
    /**
     * 不分词查询，加*（相当于sql中的%）表示模糊查询，加keyword表示查不分词数据
     */
    WILDCARD,
    /**
     * default
     * 会将搜索词分词，再与目标查询字段进行匹配，若分词中的任意一个词与目标字段匹配上，则可查询到
     */
    MATCH,
    /**
     * 范围查询，大于 ： >
     */
    GT,
    /**
     * 范围查询 大于等于 : >=
     */
    GTE,
    /**
     * 范围查询,小于 : <
     */
    LT,
    /**
     * 范围查询,小于等于 : <=
     */
    LTE,
    ;

}
