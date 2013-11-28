package org.cthul.miro.query;

import org.cthul.miro.doc.MultiValue;
import org.cthul.miro.query.adapter.*;
import org.cthul.miro.query.api.AbstractQuery;
import org.cthul.miro.query.api.QueryType;
import org.cthul.miro.query.sql.DataQuery;
import org.cthul.miro.query.template.QueryTemplate;
import org.cthul.miro.query.template.QueryTemplateProvider;

public class AbstractSelectQuery<This> extends AbstractQuery {
    
    private final SqlParser<?> sql = new SqlQueryBuilder<>(internal());

    public AbstractSelectQuery(QueryAdapter<?> adapter) {
        super(adapter);
    }

    public AbstractSelectQuery(QueryAdapter<?> adapter, QueryTemplate template) {
        super(adapter, template);
    }
    
    public AbstractSelectQuery(DBAdapter adapter, QueryTemplateProvider templateProvider) {
        super(DataQuery.SELECT, adapter, templateProvider);
    }
    
    public AbstractSelectQuery(QueryType<?> type, DBAdapter adapter, QueryTemplateProvider templateProvider) {
        super(type, adapter, templateProvider);
    }
    
    protected This self() {
        return (This) this;
    }
    
    protected SqlParser<?> sql() {
        return sql;
    }
    
    protected This select(@MultiValue String... keys) {
        for (String k: keys) {
            put(k);
        }
        return self();
    }
    
    protected This where(String key, Object... args) {
        put(key, args);
        return self();
    }
    
//    protected This where(@MultiValue String... keys) {
//        for (String k: keys) {
//            put(k);
//        }
//        return self();
//    }
}
