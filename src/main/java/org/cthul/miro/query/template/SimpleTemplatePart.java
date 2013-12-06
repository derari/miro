package org.cthul.miro.query.template;

import org.cthul.miro.query.InternalQueryBuilder;
import org.cthul.miro.query.QueryPartType;
import org.cthul.miro.query.parts.QueryPart;
import org.cthul.miro.query.parts.SimpleQueryPart;

public class SimpleTemplatePart implements QueryTemplatePart {
    
    private final Object[] required;
    private final QueryPartType type;
    private final String sql;

    public SimpleTemplatePart(QueryPartType type, String sql, Object... required) {
        this.type = type;
        this.sql = sql;
        this.required = required;
    }

    @Override
    public QueryPart addPart(Object key, InternalQueryBuilder queryBuilder) {
        Templates.requireAll(queryBuilder, required);
        QueryPart part = createPart(key, sql);
        queryBuilder.addPart(type, part);
        return part;
    }
    
    protected QueryPart createPart(Object key, String sql) {
        return new SimpleQueryPart(key, sql);
    }
}
