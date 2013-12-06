package org.cthul.miro.query.adapter;

import org.cthul.miro.query.QueryType;

/**
 * Either a {@link QueryString} or a {@link JdbcQuery}.
 */
public interface QueryAdapter<Builder extends QueryBuilder<? extends Builder>> {
    
    Builder getBuilder();
    
    QueryType<Builder> getQueryType();
}
