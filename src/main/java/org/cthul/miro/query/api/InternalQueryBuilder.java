package org.cthul.miro.query.api;

import org.cthul.miro.query.parts.QueryPart;

public interface InternalQueryBuilder extends Query {
    
    QueryType<?> getQueryType();
    
    String newKey(String hint);
    
    void addPart(QueryPartType partType, QueryPart part);
}
