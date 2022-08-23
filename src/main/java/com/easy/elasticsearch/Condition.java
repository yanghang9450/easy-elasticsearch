package com.easy.elasticsearch;

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

    public List<Condition> join(Condition... condition){
        if (condition == null) {
            throw new IllegalArgumentException("inner must query clause cannot be null");
        }else{
            Iterator<Condition> i = Arrays.stream(condition).iterator();
            mustCondition = new ArrayList<>();
            while (i.hasNext()){
                this.mustCondition.add(i.next());
            }
        }
        return this.mustCondition;
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
