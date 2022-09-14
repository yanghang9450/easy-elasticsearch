package com.easy.entity;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
/**
 * @author yanghang
 */
@Getter
public class InsertData {

    private String key;

    private Object value;

    private static List<InsertData> mustData;

    public InsertData (){}

    public InsertData(String key , Object value){
        this.key = key;
        this.value = value;
    }

    public InsertData build(Builder builder){
        this.key = builder.key;
        this.value = builder.value;
        return this;
    }

    public static Builder start(){
        mustData = new ArrayList<>();
        return new Builder();
    }

    public static void field(String key , Object value){
        mustData.add(new InsertData(key,value));
    }

    public static class Builder{
        private String key;
        private Object value;
        public Builder(){}
        public List<InsertData> end(){
            return mustData;
        }
        public Builder field(String key , Integer value){
            this.key = key;
            this.value = value;
            mustData.add(new InsertData(key,value));
            return this;
        }
        public Builder field(String key , Double value){
            this.key = key;
            this.value = value;
            mustData.add(new InsertData(key,value));
            return this;
        }
        public Builder field(String key , String value){
            this.key = key;
            this.value = value;
            mustData.add(new InsertData(key,value));
            return this;
        }
        public Builder field(String key , Float value){
            this.key = key;
            this.value = value;
            mustData.add(new InsertData(key,value));
            return this;
        }
        public Builder field(String key , Boolean value){
            this.key = key;
            this.value = value;
            mustData.add(new InsertData(key,value));
            return this;
        }
        public Builder field(String key , Byte value){
            this.key = key;
            this.value = value;
            mustData.add(new InsertData(key,value));
            return this;
        }
        public Builder field(String key , Short value){
            this.key = key;
            this.value = value;
            mustData.add(new InsertData(key,value));
            return this;
        }
        public Builder field(String key , Long value){
            this.key = key;
            this.value = value;
            mustData.add(new InsertData(key,value));
            return this;
        }
        public Builder field(String key , char value){
            this.key = key;
            this.value = value;
            mustData.add(new InsertData(key,value));
            return this;
        }
        public Builder field(String key , int value){
            this.key = key;
            this.value = value;
            mustData.add(new InsertData(key,value));
            return this;
        }
        public Builder field(String key , float value){
            this.key = key;
            this.value = value;
            mustData.add(new InsertData(key,value));
            return this;
        }
        public Builder field(String key , double value){
            this.key = key;
            this.value = value;
            mustData.add(new InsertData(key,value));
            return this;
        }
        public Builder field(String key , long value){
            this.key = key;
            this.value = value;
            mustData.add(new InsertData(key,value));
            return this;
        }
        public Builder field(String key , short value){
            this.key = key;
            this.value = value;
            mustData.add(new InsertData(key,value));
            return this;
        }
        public Builder field(String key , byte value){
            this.key = key;
            this.value = value;
            mustData.add(new InsertData(key,value));
            return this;
        }
        public Builder field(String key , boolean value){
            this.key = key;
            this.value = value;
            mustData.add(new InsertData(key,value));
            return this;
        }
    }
}
