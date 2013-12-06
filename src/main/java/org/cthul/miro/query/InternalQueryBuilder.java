package org.cthul.miro.query;

import org.cthul.miro.query.parts.QueryPart;

public interface InternalQueryBuilder extends Query {
    
    QueryType<?> getQueryType();
    
    Object newKey(String hint);
    
    boolean hasPart(Object key);
    
    void addResultAttribute(String key);
    
    void addPart(QueryPartType partType, QueryPart part);
}
