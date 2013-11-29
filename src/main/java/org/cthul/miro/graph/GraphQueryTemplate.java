package org.cthul.miro.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.cthul.miro.dsl.*;
import org.cthul.miro.map.z.MappedQueryTemplate;
import org.cthul.miro.map.z.SimpleMapping;
import org.cthul.miro.query.ZQueryBuilder.QueryPart;
import org.cthul.miro.query.ZQueryTemplate.PartTemplate;

/**
 *
 */
public class GraphQueryTemplate<Entity> extends MappedQueryTemplate<Entity> {
    
    private final List<String> keys = new ArrayList<>();

    public GraphQueryTemplate(SimpleMapping<Entity> mapping) {
        super(mapping);
    }

    public GraphQueryTemplate() {
    }
    
    public String[] getKeys() {
        if (keys.isEmpty()) {
            throw new IllegalStateException(
                    "Key attributes not configured");
        }
        return keys.toArray(new String[keys.size()]);
    }
    
    protected void key(String key) {
        keys.add(key);
    }
    
    protected void keys(String... keys) {
        this.keys.addAll(Arrays.asList(keys));
    }
    
    protected void relation(String key, View<? extends SelectByKey<?>> view, String... keyFields) {
//        PartTemplate<Entity> pt = new RelationTemplate<>(key, keyFields, view);
//        addPart(pt);
//        byDefault(key);
    }

    protected static class RelationTemplate<Entity> extends PartTemplate {
        
        private final View<? extends SelectByKey<?>> view;
        
        public RelationTemplate(String key, String[] required, Include include, View<? extends SelectByKey<?>> view) {
            super(key, include, required);
            this.view = view;
        }

        @Override
        public QueryPart createPart(String alias) {
            return null;
//            return new GraphQuery.RelationPart<>(alias, view, required);
        }
    }
    
}
