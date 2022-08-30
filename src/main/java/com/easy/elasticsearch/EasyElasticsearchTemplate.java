package com.easy.elasticsearch;

import com.easy.entity.*;
import com.easy.response.EasyResponse;
import com.easy.response.ListResult;
import com.easy.response.Result;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.DisMaxQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Component
public class EasyElasticsearchTemplate {
      private final static Logger logger = LoggerFactory.getLogger(EasyElasticsearchTemplate.class);
      private static final String Py = ".pinyin";
      private static final String StarKey = "*";
      private static final String SearchPyKey = "py";
      private static final String SearchChKey = "CH";
      private static final String SearchChPyKey = "CH_PY";
      private static final String DateDefaultFormat = "yyyy-MM-dd HH:mm:ss";
      private static final String Id = "_id";

      @Autowired
      private Client easyElasticsearch;

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
          Assert.notNull(searchBody.getIndexType(),"search index type is null");
          Assert.notNull(searchBody.getSearchTargetField(),"search field is null");
          Assert.notNull(searchBody.getSearchValue(),"search value is null");
          return EasyResponse.build(this.response(this.match(searchBody),clazz));
      }

    /**
     * elasticsearch default query function
     * @param query query condition
     * @param pageable pageable
     * @param index index
     * @param clazz class
     * @param indexType indexType
     * @param <T> Object
     * @return <T>Result<ListResult<T>>
     */
      public <T>Result<ListResult<T>> query(DisMaxQueryBuilder query, EasyRequestPageable pageable, String index, Class<T> clazz, String... indexType){
          SearchRequestBuilder builder = easyElasticsearch.prepareSearch(index).setTypes(indexType);
          if (pageable.getPage() != 0 && pageable.getSize() != 0)
              builder.setFrom((pageable.getPage() -1 )).setSize(pageable.getSize());
          if (!Objects.isNull(pageable.getSort()))
              builder.addSort(
                      pageable.getSort().getSortField(),
                      (EasySortEnum.DESC.toString().equalsIgnoreCase(pageable.getSort().getOrder().toString())) ?
                      SortOrder.DESC : SortOrder.ASC
              );
          builder.setQuery(query);
          return EasyResponse.build(this.response(builder.execute().actionGet(),clazz));
      }

    /**
     * insert or update function
     * @param insertIndex request param
     * @param <T> Object
     * @return <T>Result<T>
     */
      public <T>Result<T> save(EasyInsertIndex insertIndex){
          if (CollectionUtils.isEmpty(insertIndex.getData())){
              return EasyResponse.fail(400,"insert data not null");
          }
          Map<String, Object> collect = insertIndex.getData()
                  .stream().collect(
                          Collectors.toMap(
                                  InsertData::getKey, InsertData::getValue, (key1, key2) -> key1
                          ));
          IndexResponse indexRequestBuilder = easyElasticsearch.prepareIndex(insertIndex.getIndex(),insertIndex.getIndexType(),insertIndex.getId())
                  .setSource(collect)
                  .get();
          logger.info("insert data status : {}",indexRequestBuilder.status());
          Gson gson = new GsonBuilder()
                  .setDateFormat(DateDefaultFormat)
                  .create();
          return EasyResponse.build(new Gson().fromJson(gson.toJson(collect), new TypeToken<T>() {
          }.getType()));
      }

      public Result<String> delete(EasyDeleteIndex deleteIndex){
          this.indexIdAndConditionValid(deleteIndex.getId(),deleteIndex.getCondition());
          DisMaxQueryBuilder maxQueryBuilder = QueryBuilders.disMaxQuery();
          if (StringUtils.hasText(deleteIndex.getId())){
              maxQueryBuilder.add(QueryBuilders.termQuery(Id,deleteIndex.getId()));
          }
          for ( QueryBuilder builder : this.condition(deleteIndex.getCondition())){
              maxQueryBuilder = this.joinQuery(maxQueryBuilder,builder);
          }
          new DeleteByQueryRequestBuilder(easyElasticsearch, DeleteByQueryAction.INSTANCE)
                  .filter(maxQueryBuilder).source(deleteIndex.getIndex())
                  .execute(
                          new ActionListener<BulkByScrollResponse>() {
                              @Override
                              public void onResponse(BulkByScrollResponse response) {
                                  long deleted = response.getDeleted();
                                  logger.info("success ！ delete " + deleted + " count");
                              }
                              @Override
                              public void onFailure(Exception e) {
                                  logger.error("delete failed ！");
                                  throw new RuntimeException(e.getMessage());
                              }
                          }
                  );
        logger.info("delete data success");
        return EasyResponse.build("success");
      }

      /*public <T>Result<T> update(EasyUpdateIndex updateIndex , Class<T> clazz){
          this.indexIdAndConditionValid(updateIndex.getId(),updateIndex.getCondition());
          DisMaxQueryBuilder disMaxQueryBuilder = QueryBuilders.disMaxQuery();
          if (StringUtils.hasText(updateIndex.getId())) disMaxQueryBuilder.add(QueryBuilders.termQuery(Id,updateIndex.getId()));
          for (QueryBuilder builder : this.condition(updateIndex.getCondition())) disMaxQueryBuilder = this.joinQuery(disMaxQueryBuilder,builder);
          UpdateByQueryRequestBuilder requestBuilder = new UpdateByQueryRequestBuilder(easyElasticsearch, UpdateByQueryAction.INSTANCE)
                  .source(updateIndex.getIndex())
                  .filter(
                        disMaxQueryBuilder
                  ).script(

                  )
          return EasyResponse.build(null);
      }*/

      private void indexIdAndConditionValid(String id , List<Condition> conditions){
          if (StringUtils.isEmpty(id) && CollectionUtils.isEmpty(conditions)){
              throw new RuntimeException("delete object missing id or condition! Please set it correctly");
          }
      }

      private <T>ListResult<T> response(SearchResponse response , Class<T> clazz){
          List<T> results = new ArrayList<>();
          for (SearchHit hit : response.getHits().getHits() ){
              String sourceAsString = hit.getSourceAsString();
              if (!StringUtils.isEmpty(sourceAsString)){
                  results.add(new Gson().fromJson(sourceAsString,clazz));
              }
          }
          if (CollectionUtils.isEmpty(results)) return new ListResult<T>(new ArrayList<>(),0,0);
          return new ListResult<>(
                  results,
                  Long.valueOf(response.getHits().totalHits).intValue(),
                  Long.valueOf(response.getHits().getTotalHits()).intValue()
          );
      }
      private SearchResponse match(EasySearchBody body){
          SearchRequestBuilder builder = easyElasticsearch.prepareSearch(body.getIndex()).setTypes(body.getIndexType());
          if (body.getPageable().getPage() != 0 && body.getPageable().getSize() != 0){
              builder.setFrom(body.getPageable().getPage() - 1).setSize(body.getPageable().getSize());
          }
          DisMaxQueryBuilder query = this.disMaxQuery(body);
          if (!Objects.isNull(body.getPageable().getSort())){
              Assert.notNull(body.getPageable().getSort().getSortField(),"easy elasticsearch sort field is null");
              Assert.notNull(body.getPageable().getSort().getOrder(),"easy elasticsearch sort order is null");
              builder.addSort(
                      body.getPageable().getSort().getSortField(),
                      (EasySortEnum.DESC.toString().equalsIgnoreCase(body.getPageable().getSort().getOrder().toString())) ? SortOrder.DESC : SortOrder.ASC
              );
          }
          builder.setQuery(query);
          return builder.execute().actionGet();
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
}
