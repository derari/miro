package org.cthul.miro.query;

import org.cthul.miro.doc.MultiValue;
import org.cthul.miro.query.adapter.*;
import org.cthul.miro.query.sql.DataQuery;
import org.cthul.miro.query.template.QueryTemplate;
import org.cthul.miro.query.template.QueryTemplateProvider;

public class AbstractSelectQuery<This> extends AbstractQuery {
    
    private final SqlParser<?> sql = new SqlQueryBuilder<>(internal());

    public AbstractSelectQuery(QueryType<?> queryType) {
        super(queryType);
    }

    public AbstractSelectQuery(QueryType<?> queryType, QueryTemplate template) {
        super(queryType, template);
    }

    public AbstractSelectQuery(QueryType<?> type, QueryTemplateProvider templateProvider) {
        super(type, templateProvider);
    }

    public AbstractSelectQuery() {
        super(DataQuery.SELECT);
    }

    public AbstractSelectQuery(QueryTemplate template) {
        super(DataQuery.SELECT, template);
    }

    public AbstractSelectQuery(QueryTemplateProvider templateProvider) {
        super(DataQuery.SELECT, templateProvider);
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
