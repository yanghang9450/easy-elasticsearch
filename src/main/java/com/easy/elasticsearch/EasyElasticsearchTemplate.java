package com.easy.elasticsearch;

import com.easy.annotations.EasyIndex;
import com.easy.entity.*;
import com.easy.response.EasyResponse;
import com.easy.response.ListResult;
import com.easy.response.Result;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.DisMaxQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
/**
 * @author yanghang
 */
@Component
public class EasyElasticsearchTemplate {
      private final static Logger logger = LoggerFactory.getLogger(EasyElasticsearchTemplate.class);
      private static final String Py = ".pinyin";
      private static final String StarKey = "*";
      private static final String SearchPyKey = "py";
      private static final String SearchChKey = "ch";
      private static final String SearchChPyKey = "ch_py";
      private static final String DateDefaultFormat = "yyyy-MM-dd HH:mm:ss";
      private static final String Id = "_id";
      @Autowired
      private ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * easy elasticsearch query function
     * @param searchBody query param
     * @param clazz class
     * @param <T>  Object
     * @return <T>Result<ListResult<T>>
     */
      public <T>Result<ListResult<T>> query(EasySearchBody searchBody, Class<T> clazz){
          Assert.notNull(searchBody,"searchBody is null");
          Assert.notNull(searchBody.getIndex(),"search index is null");
          Assert.notNull(searchBody.getSearchTargetField(),"search field is null");
          Assert.notNull(searchBody.getSearchValue(),"search value is null");
          return EasyResponse.build(this.matchQuery(searchBody,clazz));
      }

    /**
     * elasticsearch default query function
     * @param query query condition
     * @param pageable pageable
     * @param index index
     * @param clazz class
     * @param <T> Object
     * @return <T>Result<ListResult<T>>
     */
      public <T>Result<ListResult<T>> query(DisMaxQueryBuilder query, EasyRequestPageable pageable, String index, Class<T> clazz){
          NativeSearchQueryBuilder nativeSearchQueryBuilder =  new NativeSearchQueryBuilder();
          if (pageable.getPage() != 0 && pageable.getSize() != 0)
              nativeSearchQueryBuilder.withPageable(PageRequest.of((pageable.getPage() -1 ),pageable.getSize()));
          else nativeSearchQueryBuilder.withPageable(PageRequest.of(0,20));
          if (!Objects.isNull(pageable.getSort()))
                nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(pageable.getSort().getSortField())
                        .order((EasySortEnum.DESC.toString().equalsIgnoreCase(pageable.getSort().getOrder().toString())) ?
                                SortOrder.DESC : SortOrder.ASC));
          nativeSearchQueryBuilder.withQuery(query);
          return EasyResponse.build(
                  this.response(
                          elasticsearchRestTemplate.search(nativeSearchQueryBuilder.build(),clazz, IndexCoordinates.of(index)),
                          clazz,
                          pageable.getSize()
                  )
          );
      }

    /**
     * insert or update function
     * @param insertIndex request param
     * @param <T> Object
     * @return <T>Result<T>
     */
      public <T>Result<T> save(EasyInsertIndex insertIndex ,Class<T> clazz){
          if (CollectionUtils.isEmpty(insertIndex.getData()) && Objects.isNull(insertIndex.getValue())){
              return EasyResponse.fail(400,"insert data not null");
          }
          if (!StringUtils.hasText(this.getIndexAnnotationValue(clazz)) && !StringUtils.hasText(insertIndex.getIndex()) ){
              return EasyResponse.fail(400,"insert index not null");
          }
          Gson gson = new GsonBuilder()
                  .setDateFormat(DateDefaultFormat)
                  .create();
          T insert = Objects.isNull(insertIndex.getValue()) ?
                  new Gson().fromJson(gson.toJson(insertIndex.getData()
                          .stream().collect(
                                  Collectors.toMap(
                                          InsertData::getKey, InsertData::getValue, (key1, key2) -> key1
                                  ))
                  ), clazz) : new Gson().fromJson(gson.toJson(insertIndex.getValue()),clazz);
          return EasyResponse.build(
                  elasticsearchRestTemplate.save(
                          insert,
                          IndexCoordinates.of(
                                  StringUtils.hasText(insertIndex.getIndex()) ?
                                          insertIndex.getIndex() : this.getIndexAnnotationValue(clazz)
                                  )
                          )
                  );
      }

    /**
     * easy elasticsearch delete object
     * @param deleteIndex delete request body
     * @return Result<String>
     */
    public <T>Result<String> delete(EasyDeleteIndex deleteIndex,Class<T> tClass){
        this.indexIdAndConditionValid(deleteIndex.getId(),deleteIndex.getCondition());
        BoolQueryBuilder builder = new BoolQueryBuilder();
        if (StringUtils.hasText(deleteIndex.getId())){
            builder.must(QueryBuilders.termQuery(Id,deleteIndex.getId()));
        }
        for ( QueryBuilder condition : this.condition(deleteIndex.getCondition())){
            builder.must(condition);
        }
        elasticsearchRestTemplate.delete(new NativeSearchQuery(builder),tClass,IndexCoordinates.of(deleteIndex.getIndex()));
        logger.info("delete data success");
        return EasyResponse.build("delete data success");
      }
      private void indexIdAndConditionValid(String id , List<Condition> conditions){
          if (!StringUtils.hasText(id) && CollectionUtils.isEmpty(conditions)){
              throw new RuntimeException("delete object missing id or condition! Please set it correctly");
          }
      }

      private <T>ListResult<T> response(SearchHits<T> hits , Class<T> clazz , int size){
          List<T> results = new ArrayList<>();
          for (SearchHit<T> hit : hits.getSearchHits() ){
              results.add(new Gson().fromJson(new Gson().toJson(hit.getContent()),clazz));
          }
          if (CollectionUtils.isEmpty(results)) return new ListResult<T>(new ArrayList<>(),0,0);
          return new ListResult<>(
                  results,
                  Long.valueOf(hits.getTotalHits()).intValue(),
                  Long.valueOf((long) Math.ceil((double) (hits.getTotalHits() / size))).intValue()
          );
      }
      private <T>ListResult<T> matchQuery(EasySearchBody body, Class<T> clazz){
          NativeSearchQueryBuilder nativeSearchQueryBuilder =  new NativeSearchQueryBuilder();
          if (body.getPageable().getPage() != 0 && body.getPageable().getSize() != 0)
              nativeSearchQueryBuilder.withPageable(PageRequest.of((body.getPageable().getPage() -1 ),body.getPageable().getSize()));
          else nativeSearchQueryBuilder.withPageable(PageRequest.of(0,20));

          DisMaxQueryBuilder query = this.disMaxQuery(body);
          if (!Objects.isNull(body.getPageable().getSort())){
              Assert.notNull(body.getPageable().getSort().getSortField(),"easy elasticsearch sort field is null");
              Assert.notNull(body.getPageable().getSort().getOrder(),"easy elasticsearch sort order is null");
              nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(body.getPageable().getSort().getSortField())
                      .order((EasySortEnum.DESC.toString().equalsIgnoreCase(body.getPageable().getSort().getOrder().toString())) ?
                              SortOrder.DESC : SortOrder.ASC));
          }
          nativeSearchQueryBuilder.withQuery(query);
          return this.response(
                  elasticsearchRestTemplate.search(nativeSearchQueryBuilder.build(),clazz, IndexCoordinates.of(body.getIndex())),
                  clazz,
                  body.getPageable().getSize()
          );
      }
     private DisMaxQueryBuilder disMaxQuery(EasySearchBody body){
         DisMaxQueryBuilder maxQueryBuilder = QueryBuilders.disMaxQuery();
         List<QueryBuilder> queryBuilders = this.condition(body.getConditions());
         for (QueryBuilder q : queryBuilders){
             maxQueryBuilder = this.joinQuery(maxQueryBuilder,q);
         }
         DisMaxQueryBuilder builder = this.participleCondition(body.getSearchTargetField(),body.getSearchValue(),body.isOpenIK(),body.isOpenPinYin());
         if (maxQueryBuilder.innerQueries().size() > 0){
             return QueryBuilders.disMaxQuery().add(
                     QueryBuilders.boolQuery().must(builder).must(maxQueryBuilder)
             );
         }else{
             return builder;
         }
     }
     private  DisMaxQueryBuilder joinQuery(DisMaxQueryBuilder oq , QueryBuilder nq){
          if (oq.innerQueries().size() > 0 ){
              return QueryBuilders.disMaxQuery().add(QueryBuilders.boolQuery().must(oq).must(nq));
          }
          return oq.add(nq);
     }
     private DisMaxQueryBuilder participleCondition(List<String> keys , String value, boolean openIk ,boolean openPinYin){
         DisMaxQueryBuilder disMaxQueryBuilder = QueryBuilders.disMaxQuery();
         keys.forEach(
                 k ->{
                     this.constitutionAnalyze(disMaxQueryBuilder,openIk,openPinYin,k,value,analyzeSearchValueType(value));
                 }
         );
         return disMaxQueryBuilder;
     }

     private List<QueryBuilder> condition(List<Condition> conditions){
         List<QueryBuilder> queryBuilders = new ArrayList<>();
         if (!CollectionUtils.isEmpty(conditions)){
             conditions.forEach(
                     c -> {
                         queryBuilders.add(this.queryBuilder(c.getMatch().toString(),c.getKey(),c.getValue()));
                     }
             );
         }
         return queryBuilders;
     }

     private String analyzeSearchValueType(String searchValue){
          if (this.isChineseCharacters(searchValue)){
              return SearchPyKey;
          }else if (this.isChineseContainsPinYin(searchValue)){
              return SearchChPyKey;
          }else {
              return SearchChKey;
          }
     }
     private void constitutionAnalyze(DisMaxQueryBuilder queryBuilder , boolean openIk , boolean openPinYin , String key , String value , String keyType){
         if (openIk && openPinYin){
             this.searchValueQueryBuilder(queryBuilder,keyType,key,value);
         }else if(openIk){
             queryBuilder.add(QueryBuilders.prefixQuery(key,value)).add(QueryBuilders.wildcardQuery(StarKey + key + StarKey,value));
         }else if (openPinYin){
             queryBuilder.add(QueryBuilders.wildcardQuery(key+Py,StarKey + value + StarKey)).add(QueryBuilders.matchPhraseQuery(key,value));
         }else  {
             queryBuilder.add(QueryBuilders.termQuery(key,value));
         }
     }
     private void searchValueQueryBuilder(DisMaxQueryBuilder queryBuilder , String searchKeyType , String key , String value){
          if (SearchPyKey.equals(searchKeyType)){
              queryBuilder.add(QueryBuilders.matchQuery(key + Py, value));
          }else if (SearchChKey.equals(searchKeyType) || SearchChPyKey.equals(searchKeyType)){
              queryBuilder.add(QueryBuilders.matchPhraseQuery(key, value)).add(QueryBuilders.matchPhraseQuery(key + Py, value));
          }else{
              queryBuilder.add(QueryBuilders.termQuery(key, value));
          }
     }
     private QueryBuilder queryBuilder(String match ,String name , Object value){
          if (Match.MATCH_PHRASE.toString().equals(match)){
              return QueryBuilders.matchPhraseQuery(name,value);
          }else if (Match.MATCH_PHRASE_PREFIX.toString().equals(match)){
            return QueryBuilders.matchPhrasePrefixQuery(name,value);
          }else if (Match.TERM.toString().equals(match)){
            return  QueryBuilders.termQuery(name,value.toString());
          }else if(Match.WILDCARD.toString().equals(match)){
              return QueryBuilders.wildcardQuery(name,value.toString());
          }else if (Match.MATCH.toString().equals(match)){
              return QueryBuilders.matchQuery(name,value);
          }else if(Match.GT.toString().equals(match)){
              return QueryBuilders.rangeQuery(name).gt(value);
          }else if(Match.GTE.toString().equals(match)){
              return QueryBuilders.rangeQuery(name).gte(value);
          }else if(Match.LT.toString().equals(match)){
              return QueryBuilders.rangeQuery(name).lt(value);
          }else if(Match.LTE.toString().equals(match)){
              return QueryBuilders.rangeQuery(name).lte(value);
          }else
              return QueryBuilders.matchQuery(name,value);
     }
     private boolean isChineseCharacters(String var){
          return var.length() == var.getBytes().length;
     }

     private boolean isChineseContainsPinYin(String var){
         String regex = ".*[a-zA-Z]+.*";
         Matcher matcher = Pattern.compile(regex).matcher(var);
         return matcher.matches();
     }

     private <T>String getIndexAnnotationValue(Class<T>  tClass){
        return tClass.getAnnotation(EasyIndex.class).name();
     }
}
