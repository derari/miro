package org.cthul.miro.query.api;

import org.cthul.miro.query.jdbc.JdbcQueryBuilder;
import org.cthul.miro.query.syntax.QueryStringBuilder;

/**
 * Either a {@link QueryStringBuilder} or a {@link JdbcQueryBuilder}.
 */
public interface QueryAdapter {
    
    QueryType getQueryType();
    
    void addPart(QueryPart part);    
}
