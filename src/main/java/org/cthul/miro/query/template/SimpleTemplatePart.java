package org.cthul.miro.query.template;

import org.cthul.miro.query.api.InternalQueryBuilder;
import org.cthul.miro.query.api.QueryPartType;
import org.cthul.miro.query.parts.QueryPart;
import org.cthul.miro.query.parts.SimpleQueryPart;

public class SimpleTemplatePart implements QueryTemplatePart {
    
    private final String[] required;
    private final QueryPartType type;
    private final String sql;

    public SimpleTemplatePart(QueryPartType type, String sql, String... required) {
        this.type = type;
        this.sql = sql;
        this.required = required;
    }

    @Override
    public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
        DataQueryTemplateProvider.requireAll(queryBuilder, required);
        QueryPart part = new SimpleQueryPart(key, sql);
        queryBuilder.addPart(type, part);
        return part;
    }
}
