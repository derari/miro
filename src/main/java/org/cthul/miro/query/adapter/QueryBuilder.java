package org.cthul.miro.query.adapter;

import org.cthul.miro.query.QueryPartType;
import org.cthul.miro.query.parts.QueryPart;

/**
 * Either a {@link QueryString} or a {@link JdbcQuery}.
 */
public interface QueryBuilder<Builder extends QueryBuilder<? extends Builder>> extends QueryAdapter<Builder> {
    
    Builder add(QueryPartType type, QueryPart part);
}
