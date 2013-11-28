package org.cthul.miro.query.api;

import org.cthul.miro.query.adapter.QueryBuilder;
import org.cthul.miro.query.parts.QueryPart;

public interface QueryPartType {
    
    void addPartTo(QueryPart part, QueryBuilder<?> query);
}
