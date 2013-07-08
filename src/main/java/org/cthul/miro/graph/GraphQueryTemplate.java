package org.cthul.miro.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.cthul.miro.dsl.*;
import org.cthul.miro.dsl.QueryBuilder.QueryPart;

/**
 *
 */
public class GraphQueryTemplate<Entity> extends QueryTemplate<Entity> {
    
    private final List<String> keys = new ArrayList<>();
    
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
        PartTemplate<Entity> pt = new RelationTemplate<>(key, keyFields, view);
        addPart(pt);
        byDefault(key);
    }

    protected static class RelationTemplate<Entity> extends PartTemplate<Entity> {
        
        private final View<? extends SelectByKey<?>> view;
        
        public RelationTemplate(String key, String[] required, View<? extends SelectByKey<?>> view) {
            super(key, required);
            this.view = view;
        }

        @Override
        public QueryPart<Entity> createPart(String alias) {
            return new GraphQuery.RelationPart<>(alias, view, required);
        }
    }
    
}
