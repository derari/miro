package org.cthul.miro.map;

import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.query.api.InternalQueryBuilder;
import org.cthul.miro.query.api.OtherQueryPart;
import org.cthul.miro.query.parts.*;
import org.cthul.miro.query.sql.DataQuery;
import org.cthul.miro.query.sql.DataQueryPart;
import org.cthul.miro.query.template.*;

public class MappedDataQueryTemplateProvider<Entity>
                extends DataQueryTemplateProvider 
                implements MappedTemplateProvider<Entity> {

    private final Mapping<Entity> mapping;

    public MappedDataQueryTemplateProvider(Mapping<Entity> mapping) {
        this.mapping = mapping;
    }

    @Override
    public Mapping<Entity> getMapping() {
        return mapping;
    }

    @Override
    protected QueryTemplate newParent(QueryTemplate parent) {
        QueryTemplate p = super.newParent(parent);
        return new MappedTemplate<>(mapping, p);
    }
    
    protected static class MappedTemplate<Entity> extends SimpleQueryTemplate {
    
        private final Mapping<Entity> mapping;

        public MappedTemplate(Mapping<Entity> mapping, QueryTemplate parent) {
            super(parent);
            this.mapping = mapping;
        }

        @Override
        protected QueryTemplatePart autoPart(String key) {
            return super.autoPart(key);
        }
    }
    
    protected static class PutEntitiesTemplate<Entity> implements QueryTemplatePart {
        private final Mapping<Entity> mapping;
        public PutEntitiesTemplate(Mapping<Entity> mapping) {
            this.mapping = mapping;
        }
        @Override
        public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
            if (queryBuilder.getQueryType() != DataQuery.INSERT) {
                queryBuilder.put("filter-by-keys");
            }
            QueryPart part = new PutEntitiesPart<>(key, queryBuilder, mapping);
            queryBuilder.addPart(OtherQueryPart.VIRTUAL, part);
            return part;
        }
    }
    
    protected static class PutEntitiesPart<Entity> extends VirtualQueryPart {
        
        private final InternalQueryBuilder queryBuilder;
        private final Mapping<Entity> mapping;

        public PutEntitiesPart(String key, InternalQueryBuilder queryBuilder, Mapping<Entity> mapping) {
            super(key);
            this.queryBuilder = queryBuilder;
            this.mapping = mapping;
        }

        @Override
        public void put(String key, Object... args) {
            switch (key) {
                case "add":
                case "addAll":
                    for (Object o: args) { 
                        Entity e = (Entity) o;
                        String k = queryBuilder.newKey("entity");
                        queryBuilder.addPart(DataQueryPart.VALUES, new EntityPart<>(k, mapping, e));
                    }
                    return;
                default:
                    super.put(key, args);
            }
        }
    }
    
    protected static class EntityPart<Entity> extends AbstractQueryPart implements ValuesQueryPart {

        private final Mapping<Entity> mapping;
        private final Entity entity;

        public EntityPart(String key, Mapping<Entity> mapping, Entity entity) {
            super(key);
            this.mapping = mapping;
            this.entity = entity;
        }
        
        @Override
        public Selector selector() {
            return new Selector();
        }
        
        public class Selector implements ValuesQueryPart.Selector {
            
            private List<String> selected;
            private List<String> filters;

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
                if (selected != null) {
                    for (String s: selected) {
                        Object o = mapping.getField(entity, s);
                        args.add(o);
                    }
                }
            }

            @Override
            public String getKey() {
                return EntityPart.this.getKey();
            }

            @Override
            public void put(String key, Object... args) {
            }

            @Override
            public void selectFilterValue(String key) {
                if (filters == null) filters = new ArrayList<>();
                filters.add(key);
            }

            @Override
            public void appendFilterValuesTo(List<Object> args) {
                if (filters != null) {
                    for (String s: filters) {
                        Object o = mapping.getField(entity, s);
                        args.add(o);
                    }
                }
            }
        }
    }
}
