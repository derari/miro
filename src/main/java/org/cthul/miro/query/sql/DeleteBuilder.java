package org.cthul.miro.query.sql;

import org.cthul.miro.query.adapter.QueryBuilder;
import org.cthul.miro.query.parts.*;

public interface DeleteBuilder<Builder extends DeleteBuilder<? extends Builder>> extends QueryBuilder<Builder> {
    
    Builder from(SqlQueryPart part);
    
    Builder join(SqlQueryPart part);
    
    Builder where(SqlQueryPart part);
    
    Builder where(AttributeQueryPart part);
    
    Builder values(ValuesQueryPart values);
}
