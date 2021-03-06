package com.easy.config;

import com.alibaba.fastjson.JSON;
import com.easy.elasticsearch.Condition;
import com.easy.elasticsearch.EasySearchBody;
import com.easy.elasticsearch.EasySortEnum;
import com.easy.elasticsearch.Match;
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

    @Autowired
    private Client easyElasticsearch;

      public <T>Result<T> query(EasySearchBody searchBody, Class<T> clazz){
          Assert.notNull(searchBody,"searchBody is null");
          Assert.notNull(searchBody.getIndex(),"search index is null");
          Assert.notNull(searchBody.getIndexType(),"search index type is null");
          Assert.notNull(searchBody.getSearchTargetField(),"search field is null");
          Assert.notNull(searchBody.getSearchValue(),"search value is null");
          SearchResponse response = this.match(searchBody);
          List<T> results = new ArrayList<>();
          for (SearchHit hit : response.getHits().getHits() ){
              String sourceAsString = hit.getSourceAsString();
              if (!StringUtils.isEmpty(sourceAsString)){
                  results.add(JSON.parseObject(sourceAsString,clazz));
              }
          }
          if (CollectionUtils.isEmpty(results)) return new Result<>(new ArrayList<>(),0,0);
          return  new Result<>(results,Long.valueOf(response.getHits().totalHits).intValue(),Long.valueOf(response.getHits().getTotalHits()).intValue());
      }

      private SearchResponse match(EasySearchBody body){
          SearchRequestBuilder builder = easyElasticsearch.prepareSearch(body.getIndex()).setTypes(body.getIndexType());
          if (body.getPage() != 0 && body.getSize() != 0){//Pagination
              builder.setFrom(body.getPage() * body.getSize()).setSize(body.getSize());
          }
          DisMaxQueryBuilder query = disMaxQuery(body.getSearchTargetField(),body.getSearchValue(),body.getConditions());
          if (!Objects.isNull(body.getSort())){
              Assert.notNull(body.getSort().getSortField(),"easy elasticsearch sort field is null");
              Assert.notNull(body.getSort().getOrder(),"easy elasticsearch sort order is null");
              if (EasySortEnum.DESC.toString().equalsIgnoreCase(body.getSort().getOrder().toString())){
                  builder.addSort(body.getSort().getSortField(), SortOrder.DESC);
              }else if (EasySortEnum.ASE.toString().equalsIgnoreCase(body.getSort().getOrder().toString())){
                  builder.addSort(body.getSort().getSortField(), SortOrder.ASC);
              }else{
                  builder.addSort(body.getSort().getSortField(), SortOrder.ASC);
              }
          }
          builder.setQuery(query);
          return builder.execute().actionGet();
      }

     private DisMaxQueryBuilder disMaxQuery(List<String> searchTargetField , String searchValue , List<Condition> conditions){
          DisMaxQueryBuilder maxQueryBuilder = QueryBuilders.disMaxQuery();
          if (!CollectionUtils.isEmpty(conditions)){
              for (Condition condition : conditions){
                        maxQueryBuilder.add(queryBuilder(condition.getMatch().toString(),condition.getKey(),condition.getValue()));
              }
          }
         DisMaxQueryBuilder builder = this.condition(searchTargetField,searchValue);
         QueryBuilder join = QueryBuilders.boolQuery().must(builder).must(maxQueryBuilder);
         DisMaxQueryBuilder query = QueryBuilders.disMaxQuery();
         query.add(join);
         return query;
     }

     private DisMaxQueryBuilder condition(List<String> keys , String value){
          DisMaxQueryBuilder disMaxQueryBuilder = QueryBuilders.disMaxQuery();
          if (this.isChineseCharacters(value)){
              for (String key : keys){
                  QueryBuilder pinyinMatch = QueryBuilders.matchPhraseQuery(key,value);
                  QueryBuilder pinyinWild = QueryBuilders.wildcardQuery(key,value);
                  disMaxQueryBuilder.add(pinyinMatch);
                  disMaxQueryBuilder.add(pinyinWild);
              }
              return disMaxQueryBuilder;
          }else {
              if (this.isChineseContainsPinYin(value)){
                for (String key : keys){
                    QueryBuilder ik = QueryBuilders.matchPhraseQuery(key,value);
                    QueryBuilder pinyin = QueryBuilders.matchPhraseQuery(key+".pinyin",value);
                    disMaxQueryBuilder.add(ik);
                    disMaxQueryBuilder.add(pinyin);
                }
                return disMaxQueryBuilder;
              }else {
                  for (String key : keys){
                      disMaxQueryBuilder.add(QueryBuilders.matchPhraseQuery(key,value));
                      disMaxQueryBuilder.add(QueryBuilders.wildcardQuery(key,value));
                  }
                  return disMaxQueryBuilder;
              }
          }
     }
     private QueryBuilder queryBuilder(String match ,String name , Object value){
          if (Match.IN.toString().equals(match)){
              return QueryBuilders.termsQuery(name,value);
          }else if (Match.AND.toString().equals(match)){
            return QueryBuilders.termsQuery(name,value);
          }else if (Match.LIKE.toString().equals(match)){
            return  QueryBuilders.wildcardQuery(name,value.toString());
          }else {
              return QueryBuilders.matchQuery(name,value);
          }
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
