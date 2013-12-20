package org.cthul.miro.graph;

import java.util.List;
import org.cthul.miro.dml.DataQueryKey;
import org.cthul.miro.map.AbstractMappedQuery;
import org.cthul.miro.map.MappedTemplateProvider;
import org.cthul.miro.query.QueryType;
import org.cthul.miro.query.parts.AbstractQueryPart;
import org.cthul.miro.query.parts.SqlQueryPart;
import org.cthul.miro.query.sql.DataQuery;
import org.cthul.miro.query.template.UniqueKey;

public class GraphSelectQuery<Entity> extends AbstractMappedQuery<Entity> {
    
    private OrderedResult<Entity> resultBuilder = null;
    private FilterPart filterPart = null;

    public GraphSelectQuery(MappedTemplateProvider<Entity> templateProvider, Graph graph, Object... fields) {
        this(DataQuery.SELECT, templateProvider, graph, fields);
    }
    
    public GraphSelectQuery(QueryType<?> type, MappedTemplateProvider<Entity> templateProvider, Graph graph, Object... fields) {
        super(type, templateProvider);
        setGraph(graph);
        put(DataQueryKey.PUT_KEYS);
        put(DataQueryKey.PUT_STRINGS, fields);
    }
    
    public void selectByFields(String... fields) {
        resultBuilder = new OrderedResult<>(mappedProvider.getMapping(), fields);
        filterPart = new FilterPart(FILTER_PART_KEY, fields);
    }
    
    private static final Object FILTER_PART_KEY = new UniqueKey("filter-by-foreign-key");
    
    private class FilterPart extends AbstractQueryPart implements SqlQueryPart {
        
        private final String[] fields;

        public FilterPart(Object key, String[] fields) {
            super(key);
            this.fields = fields;
        }

        @Override
        public void appendSqlTo(StringBuilder sqlBuilder) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void appendArgsTo(List<Object> args) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
    
//    public GraphSelectQuery<Entity> into(Entity... entities) {
//        EntityGraphAdapter<Entity> ga = mappedProvider.getGraphAdapter();
//        Object[] empty = {};
//        put(DataQueryKey.ALL_KEY_DEPENDENCIES);
//        addToGraph(entities);
//        for (Entity e: entities) {
//            Object[] key = ga.getKey(e, empty);
//            put2(DataQueryKey.KEYS_IN, DataQuerySubkey.ADD, key);
//        }
//        return this;
//    }
//    
}
