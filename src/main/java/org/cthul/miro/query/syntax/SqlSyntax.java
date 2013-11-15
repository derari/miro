package org.cthul.miro.query.syntax;

public interface SqlSyntax {
    
    SqlBuilder newQuery(QueryType queryType);
}
