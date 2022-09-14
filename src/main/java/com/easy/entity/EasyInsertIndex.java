package com.easy.entity;

import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
/**
 * @author yanghang
 */
@Getter
public class EasyInsertIndex {
    @NotNull(message = "index not null")
    private final String index;

    protected List<InsertData> data;

    protected Object value;
    public static Builder builder(){
        return new Builder();
    }

    public EasyInsertIndex (Builder builder){
        this.index = builder.index;
        this.data = builder.data;
        this.value = builder.value;
    }
    public static class Builder {
        private String index;
        private List<InsertData> data;
        protected Object value;
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
        public Builder value(Object value){
            this.value = value;
            return this;
        }
        public Builder data(List<InsertData> data){
            this.data = data;
            return this;
        }
        public Builder(){

        }

        public Builder(String index, List<InsertData> data,Object value) {
            this.index = index;
            this.data = data;
            this.value = value;
        }
        public EasyInsertIndex build(){
            return new EasyInsertIndex(new Builder(this.index,this.data,this.value));
        }
    }
}
