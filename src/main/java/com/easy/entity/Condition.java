package com.easy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
