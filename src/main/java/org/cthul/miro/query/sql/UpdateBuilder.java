package org.cthul.miro.query.sql;

import org.cthul.miro.query.adapter.QueryBuilder;
import org.cthul.miro.query.parts.*;

public interface UpdateBuilder<Builder extends UpdateBuilder<? extends Builder>> extends QueryBuilder<Builder> {
    
    Builder update(SqlQueryPart part);
    
    Builder join(SqlQueryPart part);
    
    Builder set(SqlQueryPart part);
    
    Builder set(AttributeQueryPart part);
    
    Builder where(SqlQueryPart part);
    
    Builder where(AttributeQueryPart part);
    
    Builder values(ValuesQueryPart values);
}
