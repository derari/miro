package org.cthul.miro.query.api;

import java.util.List;
import org.cthul.miro.query.syntax.QueryType;
import org.cthul.miro.query.syntax.SqlBuilder;
import org.cthul.miro.query.syntax.SqlSyntax;

public abstract class QueryBuilder {

    private final SqlBuilder sqlBuilder;

    public QueryBuilder(SqlBuilder sqlBuilder) {
        this.sqlBuilder = sqlBuilder;
    }

    public QueryBuilder(SqlSyntax sqlSyntax, QueryType queryType) {
        this(sqlSyntax.newQuery(queryType));
    }
    
    protected void addPart(QueryPart part) {
        sqlBuilder.addPart(part);
    }
    
    protected void buildQuery(StringBuilder sql, List<Object> args) {
        
    }
}
