package com.easy.elasticsearch;

import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Getter
public class EasySearchBody {
    private final String index;
    private final String indexType;
    private final List<String> searchTargetField;
    private final String searchValue;
    private final EasyRequestPageable pageable;
    private final List<Condition> conditions;
    private final boolean openIK;
    private final boolean openPinYin;
    public static Builder builder(){
        return new Builder();
    }
    public EasySearchBody(Builder builder){
        this.index = builder.index;
        this.indexType = builder.indexType;
        this.searchTargetField = builder.searchTargetField;
        this.searchValue = builder.searchValue;
        this.pageable = builder.pageable;
        this.conditions = builder.conditions;
        this.openIK = builder.openIK;
        this.openPinYin = builder.openPinYin;
    }
    @Data
    public static class Builder {
        private String index;
        private String indexType;
        private List<String> searchTargetField;
        private String searchValue;
        private EasyRequestPageable pageable;
        private List<Condition> conditions;
        private boolean openIK;
        private boolean openPinYin;
        public Builder index(String index) {
            this.index = index ;
            return this;
        }
        public Builder indexType(String indexType) {
            this.indexType = indexType ;
            return this;
        }
        public Builder searchTargetField(String... searchTargetField){
            List<String> fields = new ArrayList<>();
            Iterator<String> i = Arrays.stream(searchTargetField).iterator();
            while (i.hasNext()){
                fields.add(i.next());
            }
            this.searchTargetField = fields;
            return this;
        }
        public Builder searchValue(String searchValue) {
            this.searchValue = searchValue ;
            return this;
        }
        public Builder pageable(EasyRequestPageable pageable) {
            this.pageable = pageable ;
            return this;
        }
        public Builder conditions(Condition... condition) {
            List<Condition> conditions = new ArrayList<>();
            Iterator<Condition> i = Arrays.stream(condition).iterator();
            while (i.hasNext()){
                conditions.add(i.next());
            }
            this.conditions = conditions ;
            return this;
        }
        public Builder openIK(boolean openIK) {
            this.openIK = openIK;
            return this;
        }
        public Builder openPinYin(boolean openPinYin) {
            this.openPinYin = openPinYin;
            return this;
        }

        public Builder(){

        }
        public Builder(String index, String indexType, List<String> searchTargetField, String searchValue, EasyRequestPageable pageable, List<Condition> conditions, boolean openIK, boolean openPinYin) {
            this.index = index;
            this.indexType = indexType;
            this.searchTargetField = searchTargetField;
            this.searchValue = searchValue;
            this.pageable = pageable;
            this.conditions = conditions;
            this.openIK = openIK;
            this.openPinYin = openPinYin;
        }
        public EasySearchBody build(){
            return new EasySearchBody(new Builder(this.index,this.indexType,this.searchTargetField,this.searchValue,this.pageable,this.conditions,this.openIK,this.openPinYin));
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "index='" + index + '\'' +
                    ", indexType='" + indexType + '\'' +
                    ", searchTargetField=" + searchTargetField +
                    ", searchValue='" + searchValue + '\'' +
                    ", pageable=" + pageable +
                    ", conditions=" + conditions +
                    ", openIK=" + openIK +
                    ", openPinYin=" + openPinYin +
                    '}';
        }
    }
    @Override
    public String toString() {
        return "EasySearchBody{" +
                "index='" + index + '\'' +
                ", indexType='" + indexType + '\'' +
                ", searchTargetField=" + searchTargetField +
                ", searchValue='" + searchValue + '\'' +
                ", pageable=" + pageable +
                ", conditions=" + conditions +
                ", openIK=" + openIK +
                ", openPinYin=" + openPinYin +
                '}';
    }
}
