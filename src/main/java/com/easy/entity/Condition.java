package com.easy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
/**
 * @author yanghang
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Condition {
    private Match match;
    private String key;
    private String value;
    protected List<Condition> mustCondition;
    protected static List<Condition> list(Condition ... condition){
        List<Condition> conditions = new ArrayList<>();
        Iterator<Condition> i = Arrays.stream(condition).iterator();
        while (i.hasNext()){
            conditions.add(i.next());
        }
        return conditions;
    }
    public List<Condition> toList(){
        mustCondition = new ArrayList<>();
        mustCondition.add(this);
        return this.mustCondition;
    }
    public Condition(String key, String value ,Match match){
        this.key = key ;
        this.value = value;
        this.match = match;
        if (CollectionUtils.isEmpty(mustCondition)){
            mustCondition = new ArrayList<>();
        }
        mustCondition.add(this);
    }
    public void start(){
        if (CollectionUtils.isEmpty(this.mustCondition)) this.mustCondition = new ArrayList<>();
    }
    public Condition put(String key, String value ,Match match){
        Condition c = new Condition(key,value,match);
        this.mustCondition.add(c);
        return c;
    }

    public Condition param(String key, String value ,Match match){
        return new Condition(key,value,match);
    }
    @Override
    public String toString() {
        return "Condition{" +
                "match=" + match +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", mustCondition=" + mustCondition +
                '}';
    }
}
