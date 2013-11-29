package org.cthul.miro.view;

import org.cthul.miro.map.MappedDataQueryTemplateProvider;
import org.cthul.miro.map.z.MappedQueryTemplate;
import org.cthul.miro.query.api.QueryType;
import org.cthul.miro.result.ResultBuilder;

public class Views {
    
    
    public static interface QueryFactory<Entity, Query> {
        
        Query newQuery(String[] args);
    }
    
    protected static class ReflectiveQueryFactory<Entity, Query> implements QueryFactory<Entity, Query> {

        @Override
        public Query newQuery(String[] args) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
    
    private static enum FactoryArg {
        
        QUERY_TYPE,
        TEMPLATE,
        TEMPLATE_PROVIDER,
        RESULT_BUILDER;
        
        private final Class<?> type;

        private FactoryArg(Class<?> type) {
            this.type = type;
        }
        
        public Object get(QueryType<?> type, MappedDataQueryTemplateProvider<?> provider, ResultBuilder<?,?> resultBuilder) {
            throw new UnsupportedOperationException("abstract");
        }
    }
}
