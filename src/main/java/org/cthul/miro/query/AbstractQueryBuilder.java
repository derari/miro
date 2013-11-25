package org.cthul.miro.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cthul.miro.query.api.QueryPart;
import org.cthul.miro.query.api.QueryType;
import org.cthul.miro.query.syntax.QueryStringBuilder;
import org.cthul.miro.query.syntax.QuerySyntax;

public abstract class AbstractQueryBuilder {

    private final QueryStringBuilder sqlBuilder;
    private final Map<String, QueryPart> parts = new HashMap<>();

    public AbstractQueryBuilder(QueryStringBuilder sqlBuilder) {
        this.sqlBuilder = sqlBuilder;
    }

    public AbstractQueryBuilder(QuerySyntax sqlSyntax, QueryType queryType) {
        this(sqlSyntax.newQueryStringBuilder(queryType));
    }
    
    protected synchronized void addPart(String key, QueryPart part) {
        parts.put(key, part);
        sqlBuilder.addPart(part);
    }
    
    protected String getQueryString() {
        return sqlBuilder.getQueryString();
    }
    
    protected int getBatchCount() {
        return sqlBuilder.getBatchCount();
    }
    
    protected List<Object> getArguments(int batch) {
        return sqlBuilder.getArguments(batch);
    }
}
