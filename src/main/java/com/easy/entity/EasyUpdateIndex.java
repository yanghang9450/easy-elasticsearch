package com.easy.entity;

import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Deprecated
public class EasyUpdateIndex {

    private final String id;
    @NotNull(message = "index not null")
    private final String index;
    private final List<Condition> condition;

    public static Builder builder(){
        return new Builder();
    }

    public EasyUpdateIndex(Builder builder){
        this.id = builder.id;
        this.index = builder.index;
        this.condition = builder().condition;
    }
    public static class Builder {
        private String id;
        private String index;
        private List<Condition> condition;
        public Builder id(String id) {
            this.id = id ;
            return this;
        }
        public Builder index(String index) {
            this.index = index ;
            return this;
        }
        public Builder(){

        }
        public Builder condition(Condition... condition) {
            this.condition = Condition.list(condition) ;
            return this;
        }
        public Builder(String id ,String index,List<Condition> condition) {
            this.id = id;
            this.index = index;
            this.condition = condition;
        }
        public EasyUpdateIndex build(){
            return new EasyUpdateIndex(new Builder(this.id,this.index,this.condition));
        }
    }
}
