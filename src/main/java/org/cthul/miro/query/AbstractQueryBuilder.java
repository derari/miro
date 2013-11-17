package org.cthul.miro.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cthul.miro.query.api.QueryPart;
import org.cthul.miro.query.syntax.QueryType;
import org.cthul.miro.query.syntax.SqlBuilder;
import org.cthul.miro.query.syntax.SqlSyntax;

public abstract class AbstractQueryBuilder {

    private final SqlBuilder sqlBuilder;
    private final Map<String, QueryPart> parts = new HashMap<>();

    public AbstractQueryBuilder(SqlBuilder sqlBuilder) {
        this.sqlBuilder = sqlBuilder;
    }

    public AbstractQueryBuilder(SqlSyntax sqlSyntax, QueryType queryType) {
        this(sqlSyntax.newQuery(queryType));
    }
    
    protected synchronized void addPart(String key, QueryPart part) {
        parts.put(key, part);
        sqlBuilder.addPart(part);
    }
    
    protected String getQueryString() {
        return sqlBuilder.getQueryString();
    }
    
    protected List<Object> getArguments() {
        return sqlBuilder.getArguments();
    }
}
