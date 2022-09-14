package com.easy.entity;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
/**
 * @author yanghang
 */
@Getter
public class EasySearchBody {
    private final String index;
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
        this.searchTargetField = builder.searchTargetField;
        this.searchValue = builder.searchValue;
        this.pageable = builder.pageable;
        this.conditions = builder.conditions;
        this.openIK = builder.openIK;
        this.openPinYin = builder.openPinYin;
    }
    public static class Builder {
        private String index;
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
            this.conditions = Condition.list(condition) ;
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
        public Builder(String index, List<String> searchTargetField, String searchValue, EasyRequestPageable pageable, List<Condition> conditions, boolean openIK, boolean openPinYin) {
            this.index = index;
            this.searchTargetField = searchTargetField;
            this.searchValue = searchValue;
            this.pageable = pageable;
            this.conditions = conditions;
            this.openIK = openIK;
            this.openPinYin = openPinYin;
        }
        public EasySearchBody build(){
            return new EasySearchBody(new Builder(this.index,this.searchTargetField,this.searchValue,this.pageable,this.conditions,this.openIK,this.openPinYin));
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "index='" + index + '\'' +
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
                ", searchTargetField=" + searchTargetField +
                ", searchValue='" + searchValue + '\'' +
                ", pageable=" + pageable +
                ", conditions=" + conditions +
                ", openIK=" + openIK +
                ", openPinYin=" + openPinYin +
                '}';
    }
}
