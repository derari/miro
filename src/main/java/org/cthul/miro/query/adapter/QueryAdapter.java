package org.cthul.miro.query.adapter;

import org.cthul.miro.query.api.QueryType;

/**
 * Either a {@link QueryString} or a {@link JdbcQuery}.
 */
public interface QueryAdapter<Builder> {
    
    Builder getBuilder();
    
    QueryType<Builder> getQueryType();
}
