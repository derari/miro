package org.cthul.miro.query.sql;

import org.cthul.miro.query.adapter.QueryAdapter;
import org.cthul.miro.query.parts.*;

public interface UpdateQueryBuilder extends QueryAdapter<UpdateQueryBuilder> {
    
    UpdateQueryBuilder update(QueryPart part);
    
    UpdateQueryBuilder join(QueryPart part);
    
    UpdateQueryBuilder set(QueryPart part);
    
    UpdateQueryBuilder set(String attribute);
    
    UpdateQueryBuilder where(QueryPart part);
    
    UpdateQueryBuilder where(String attribute);
    
    UpdateQueryBuilder values(ValuesQueryPart values);
}
