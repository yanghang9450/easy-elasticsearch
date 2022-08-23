package com.easy.config;

import com.easy.elasticsearch.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.DisMaxQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EasyElasticsearchTemplate {

      private static final String Py = ".pinyin";
      private static final String STAR_KEY = "*";
      private static final String SEARCH_PY_KEY = "PY";
      private static final String SEARCH_CH_KEY = "CH";
      private static final String SEARCH_CH_PY_KEY = "CH_PY";
      @Autowired
      private Client easyElasticsearch;

      public <T>Result<T> query(EasySearchBody searchBody, Class<T> clazz){
          Assert.notNull(searchBody,"searchBody is null");
          Assert.notNull(searchBody.getIndex(),"search index is null");
          Assert.notNull(searchBody.getIndexType(),"search index type is null");
          Assert.notNull(searchBody.getSearchTargetField(),"search field is null");
          Assert.notNull(searchBody.getSearchValue(),"search value is null");
          return this.response(this.match(searchBody),clazz);
      }

      public <T>Result<T> query(DisMaxQueryBuilder query, EasyRequestPageable pageable, String index,Class<T> clazz, String... indexType){
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
          return this.response(builder.execute().actionGet(),clazz);
      }

      private <T>Result<T> response(SearchResponse response , Class<T> clazz){
          List<T> results = new ArrayList<>();
          for (SearchHit hit : response.getHits().getHits() ){
              String sourceAsString = hit.getSourceAsString();
              if (!StringUtils.isEmpty(sourceAsString)){
                  results.add(new Gson().fromJson(sourceAsString,clazz));
              }
          }
          if (CollectionUtils.isEmpty(results)) return new Result<>(new ArrayList<>(),0,0);
          return  new Result<>(results,Long.valueOf(response.getHits().totalHits).intValue(),Long.valueOf(response.getHits().getTotalHits()).intValue());
      }
      private SearchResponse match(EasySearchBody body){
          SearchRequestBuilder builder = easyElasticsearch.prepareSearch(body.getIndex()).setTypes(body.getIndexType());
          if (body.getPageable().getPage() != 0 && body.getPageable().getSize() != 0){//Pagination
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
          List<QueryBuilder> queryBuilders = new ArrayList<>();
          if (!CollectionUtils.isEmpty(body.getConditions())){
              body.getConditions().forEach(
                      c -> {
                          queryBuilders.add(this.queryBuilder(c.getMatch().toString(),c.getKey(),c.getValue()));
                      }
              );
          }
         for (QueryBuilder q : queryBuilders){
             maxQueryBuilder = this.joinQuery(maxQueryBuilder,q);
         }
         DisMaxQueryBuilder builder = this.condition(body.getSearchTargetField(),body.getSearchValue(),body.isOpenIK(),body.isOpenPinYin());
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
     private DisMaxQueryBuilder condition(List<String> keys , String value, boolean openIk ,boolean openPinYin){
         DisMaxQueryBuilder disMaxQueryBuilder = QueryBuilders.disMaxQuery();
         keys.forEach(
                 k ->{
                     this.constitutionAnalyze(disMaxQueryBuilder,openIk,openPinYin,k,value,analyzeSearchValueType(value));
                 }
         );
         return disMaxQueryBuilder;
     }

     private String analyzeSearchValueType(String searchValue){
          if (this.isChineseCharacters(searchValue)){
              return SEARCH_PY_KEY;
          }else if (this.isChineseContainsPinYin(searchValue)){
              return SEARCH_CH_PY_KEY;
          }else {
              return SEARCH_CH_KEY;
          }
     }
     private DisMaxQueryBuilder constitutionAnalyze(DisMaxQueryBuilder queryBuilder , boolean openIk , boolean openPinYin , String key , String value , String keyType){
         if (openIk && openPinYin){
             return searchValueQueryBuilder(queryBuilder,keyType,key,value);
         }else if(openIk){
             return queryBuilder.add(QueryBuilders.prefixQuery(key,value)).add(QueryBuilders.wildcardQuery(STAR_KEY + key + STAR_KEY,value));
         }else if (openPinYin){
             return queryBuilder.add(QueryBuilders.wildcardQuery(key+Py,STAR_KEY+value+STAR_KEY)).add(QueryBuilders.matchPhraseQuery(key,value));
         }else  {
             return queryBuilder.add(QueryBuilders.termQuery(key,value));
         }
     }
     private DisMaxQueryBuilder searchValueQueryBuilder(DisMaxQueryBuilder queryBuilder , String searchKeyType , String key , String value){
          if (SEARCH_PY_KEY.equals(searchKeyType)){
              return queryBuilder.add(QueryBuilders.matchQuery(key + Py, value));
          }else if (SEARCH_CH_KEY.equals(searchKeyType) || SEARCH_CH_PY_KEY.equals(searchKeyType)){
              return queryBuilder.add(QueryBuilders.matchPhraseQuery(key,value)).add(QueryBuilders.matchPhraseQuery(key+Py,value));
          }else{
              return queryBuilder.add(QueryBuilders.termQuery(key,value));
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
