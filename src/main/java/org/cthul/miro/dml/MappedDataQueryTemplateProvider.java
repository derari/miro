package org.cthul.miro.dml;

import org.cthul.miro.query.QueryType;
import org.cthul.miro.query.InternalQueryBuilder;
import org.cthul.miro.query.OtherQueryPart;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import org.cthul.miro.graph.EntityGraphAdapter;
import org.cthul.miro.map.EntityPart;
import org.cthul.miro.map.MappedTemplate;
import org.cthul.miro.map.MappedTemplateProvider;
import org.cthul.miro.map.Mapping;
import org.cthul.miro.query.parts.*;
import org.cthul.miro.query.sql.DataQuery;
import org.cthul.miro.query.sql.DataQueryPart;
import org.cthul.miro.query.template.*;
import org.cthul.miro.result.EntityBuilderBase;
import org.cthul.miro.result.EntityFactory;
import static org.cthul.miro.query.template.Templates.*;
import static org.cthul.miro.dml.DataQueryKey.*;
import static org.cthul.miro.dml.DataQuerySubkey.*;
import static org.cthul.miro.dml.MappedDataQueryKey.*;

public class MappedDataQueryTemplateProvider<Entity>
                extends DataQueryTemplateProvider 
                implements MappedTemplateProvider<Entity> {

    private final Mapping<Entity> mapping;
    private final GraphAdapter graphAdapter;

    public MappedDataQueryTemplateProvider(Mapping<Entity> mapping) {
        this.mapping = mapping;
        this.graphAdapter = new GraphAdapter();
    }

    @Override
    public MappedTemplate getTemplate(QueryType<?> queryType) {
        return (MappedTemplate) super.getTemplate(queryType);
    }

    @Override
    protected QueryTemplate customize(QueryTemplate template) {
        QueryTemplate t = super.customize(template);
        return new MappedTemplateWrapper(t);
    }

    @Override
    public Mapping<Entity> getMapping() {
        return mapping;
    }

    @Override
    public EntityGraphAdapter<Entity> getGraphAdapter() {
        return graphAdapter;
    }

    @Override
    protected QueryTemplate newParent(QueryTemplate parent) {
        QueryTemplate p = super.newParent(parent);
        return new MappedParent<>(p);
    }
    
    protected class GraphAdapter extends EntityBuilderBase implements EntityGraphAdapter<Entity> {

        private String[] keys = null;
        
        @Override
        public Object[] getKey(Entity e, Object[] array) {
            if (keys == null) {
                keys = getKeys().toArray(new String[0]);
            }
            if (array.length < keys.length) {
                array = Arrays.copyOf(array, keys.length);
            }
            final String[] k = keys;
            for (int i = 0; i < k.length; i++) {
                array[i] = mapping.getField(e, k[i]);
            }
            return array;
        }

        @Override
        public EntityFactory<Entity> newFactory(ResultSet rs) throws SQLException {
            return mapping.newFactory(rs);
        }

        @Override
        public Entity[] newArray(int length) {
            return mapping.newArray(length);
        }

        @Override
        public KeyReader newKeyReader(final ResultSet rs) throws SQLException {
            final int[] indices = getFieldIndices(rs, keys);
            return new KeyReader() {
                @Override
                public Object[] getKey(Object[] array) throws SQLException {
                    return getFields(rs, indices, array);
                }
            };
        }
    }
    
    protected class MappedTemplateWrapper extends AbstractQueryTemplate implements MappedTemplate<Entity> {

        public MappedTemplateWrapper(QueryTemplate parent) {
            super(parent);
        }

        @Override
        public Mapping<Entity> getMapping() {
            return mapping;
        }
    }
    
    protected class MappedParent<Entity> extends AbstractQueryTemplate {
    
        public MappedParent(QueryTemplate parent) {
            super(parent);
        }

        @Override
        protected QueryTemplatePart autoPart(Object key) {
            switch (asMappedDataQueryKey(key)) {
                case ENTITIES:
                    return new IncludeEntitiesTemplate();
                case SELECT_ENTITIES:
                    return new SelectEntitiesTemplate();
                case INSERT_ENTITIES:
                case UPDATE_ENTITIES:
                case DELETE_ENTITIES:
                    return new PutEntitiesTemplate();
                    
            }
            return super.autoPart(key);
        }
    }
    
    protected static class IncludeEntitiesTemplate extends AbstractTemplatePart {
        @Override
        public QueryPart addPart(Object key, InternalQueryBuilder queryBuilder) {
            QueryPart part = new IncludeEntitiesPart(queryBuilder, getProxyKey(queryBuilder.getQueryType()), key);
            queryBuilder.addPart(OtherQueryPart.VIRTUAL, part);
            return part;
        }
        
        private MappedDataQueryKey getProxyKey(QueryType<?> type) {
            switch(DataQuery.Type.get(type)) {
                case SELECT:
                    return SELECT_ENTITIES;
                case INSERT:
                    return INSERT_ENTITIES;
                case UPDATE:
                    return UPDATE_ENTITIES;
                case DELETE:
                    return DELETE_ENTITIES;
            }
            return null;
        }
    }
    
    protected static class IncludeEntitiesPart extends AbstractQueryPart {
        private final InternalQueryBuilder queryBuilder;
        private final MappedDataQueryKey entitiesKey;

        public IncludeEntitiesPart(InternalQueryBuilder queryBuilder, MappedDataQueryKey entitiesKey, Object key) {
            super(key);
            this.queryBuilder = queryBuilder;
            this.entitiesKey = entitiesKey;
        }

        @Override
        public void put(Object key, Object... args) {
            queryBuilder.put2(entitiesKey, key, args);
        }
    }
    
    protected class SelectEntitiesTemplate extends ConfigurationTemplate {
        @Override
        public QueryPart addPart(Object key, InternalQueryBuilder queryBuilder) {
            queryBuilder.put(ALL_KEY_DEPENDENCIES);
            return super.addPart(key, queryBuilder);
        }
        @Override
        protected boolean put(InternalQueryBuilder queryBuilder, Object newKey, Object key, Object... args) {
            Entity e = (Entity) key;
            Object[] keys = getGraphAdapter().getKey(e, NO_DEPENDENCIES);
            queryBuilder.put2(KEYS_IN, ADD, keys);
            return true;
        }
    }
    
    protected class PutEntitiesTemplate extends ConfigurationTemplate {
        @Override
        public QueryPart addPart(Object key, InternalQueryBuilder queryBuilder) {
            if (queryBuilder.getQueryType() != DataQuery.INSERT) {
                queryBuilder.put(FILTER_BY_KEYS);
            }
            return super.addPart(key, queryBuilder);
        }

        @Override
        protected boolean handlePut(InternalQueryBuilder queryBuilder, Object partKey, Object key, Object... args) {
            switch (asDataQuerySubkey(key)) {
                case ADD:
                case ADD_ALL:
                    for (Object a: args) {
                        Entity e = (Entity) a;
                        Object k = queryBuilder.newKey("entity");
                        queryBuilder.addPart(DataQueryPart.VALUES, new EntityPart<>(k, mapping, e));
                    }
                    return true;
            }
            return true;
        }
    }
}
