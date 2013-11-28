package org.cthul.miro.query.api;

import org.cthul.miro.query.parts.QueryPart;

public interface InternalQueryBuilder {
    
    QueryType<?> getQueryType();
    
    void require(String key);
    
    String newKey(String hint);
    
    void addPart(QueryPartType partType, QueryPart part);
}
