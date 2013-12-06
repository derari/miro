package org.cthul.miro.query;

import org.cthul.miro.query.adapter.QueryBuilder;
import org.cthul.miro.query.parts.QueryPart;

public enum OtherQueryPart implements QueryPartType {
    
    VIRTUAL;

    @Override
    public void addPartTo(QueryPart part, QueryBuilder<?> query) {
    }
}
