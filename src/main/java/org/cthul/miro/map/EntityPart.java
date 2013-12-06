package org.cthul.miro.map;

import java.util.ArrayList;
import org.cthul.miro.query.parts.*;
import java.util.Arrays;
import java.util.List;

public class EntityPart<Entity> extends AbstractQueryPart implements ValuesQueryPart {
    
    private final Mapping<Entity> mapping;
    private final Entity entity;

    public EntityPart(Object key, Mapping<Entity> mapping, Entity entity) {
        super(key);
        this.mapping = mapping;
        this.entity = entity;
    }

    @Override
    public Selector selector() {
        return new Selector(getKey());
    }

    protected class Selector extends AbstractQueryPart implements ValuesQueryPart.Selector {
        
        private List<String> selected = null;
        private List<String> filters = null;

        public Selector(Object key) {
            super(key);
        }
        
        @Override
        public void selectAttribute(String attribute, String alias) {
            if (selected == null) selected = new ArrayList<>();
            selected.add(attribute);
        }

        @Override
        public void appendSqlTo(StringBuilder sqlBuilder) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void appendArgsTo(List<Object> args) {
            appendValuesTo(selected, args);
        }

        @Override
        public void selectFilterValue(String key) {
            if (filters == null) filters = new ArrayList<>();
            filters.add(key);
        }

        @Override
        public void appendFilterValuesTo(List<Object> args) {
            appendValuesTo(filters, args);
        }
        
        private void appendValuesTo(List<String> attributes, List<Object> args) {
            if (attributes == null) return;
            for (String a: attributes) {
                args.add(mapping.getField(entity, a));
            }
        }
    }
}

