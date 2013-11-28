package org.cthul.miro.query.sql;

import org.cthul.miro.query.adapter.QueryBuilder;
import org.cthul.miro.query.parts.*;

public interface UpdateBuilder<Builder extends UpdateBuilder<? extends Builder>> extends QueryBuilder<Builder> {
    
    Builder update(QueryPart part);
    
    Builder join(QueryPart part);
    
    Builder set(QueryPart part);
    
    Builder set(AttributeQueryPart part);
    
    Builder where(QueryPart part);
    
    Builder where(AttributeQueryPart part);
    
    Builder values(ValuesQueryPart values);
}
