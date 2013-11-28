package org.cthul.miro.query.sql;

import org.cthul.miro.query.adapter.QueryBuilder;
import org.cthul.miro.query.parts.QueryPart;

public interface SelectBuilder<Builder extends SelectBuilder<? extends Builder>> extends QueryBuilder<Builder> {
    
    Builder select(QueryPart part);
    
    Builder from(QueryPart part);
    
    Builder join(QueryPart part);
    
    Builder where(QueryPart part);
    
    Builder groupBy(QueryPart part);
    
    Builder having(QueryPart part);
    
    Builder orderBy(QueryPart part);
}
