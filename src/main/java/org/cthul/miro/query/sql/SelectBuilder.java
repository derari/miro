package org.cthul.miro.query.sql;

import org.cthul.miro.query.adapter.QueryBuilder;
import org.cthul.miro.query.parts.SqlQueryPart;

public interface SelectBuilder<Builder extends SelectBuilder<? extends Builder>> extends QueryBuilder<Builder> {
    
    Builder select(SqlQueryPart part);
    
    Builder from(SqlQueryPart part);
    
    Builder join(SqlQueryPart part);
    
    Builder where(SqlQueryPart part);
    
    Builder groupBy(SqlQueryPart part);
    
    Builder having(SqlQueryPart part);
    
    Builder orderBy(SqlQueryPart part);
}
