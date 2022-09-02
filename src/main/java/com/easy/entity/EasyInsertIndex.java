package com.easy.entity;

import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Getter
public class EasyInsertIndex {
    @NotNull(message = "id not null")
    private final String id;
    @NotNull(message = "index not null")
    private final String index;

    protected List<InsertData> data;
    public static Builder builder(){
        return new Builder();
    }

    public EasyInsertIndex (Builder builder){
        this.id = builder.id;
        this.index = builder.index;
        this.data = builder.data;
    }
    public static class Builder {
        private String id;
        private String index;
        private List<InsertData> data;
        public Builder id(String id) {
            this.id = id ;
            return this;
        }
        public Builder index(String index) {
            this.index = index ;
            return this;
        }
        public Builder data(InsertData... data) {
            List<InsertData> ds = new ArrayList<>();
            Iterator<InsertData> i = Arrays.stream(data).iterator();
            while (i.hasNext()){
                ds.add(i.next());
            }
            this.data = ds;
            return this;
        }
        public Builder data(List<InsertData> data){
            this.data = data;
            return this;
        }
        public Builder(){

        }

        public Builder(String id ,String index, List<InsertData> data) {
            this.id = id;
            this.index = index;
            this.data = data;
        }
        public EasyInsertIndex build(){
            return new EasyInsertIndex(new Builder(this.id,this.index,this.data));
        }
    }
}
