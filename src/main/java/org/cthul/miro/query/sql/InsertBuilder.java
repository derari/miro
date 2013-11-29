package org.cthul.miro.query.sql;

import org.cthul.miro.query.adapter.QueryBuilder;
import org.cthul.miro.query.parts.*;

public interface InsertBuilder<Builder extends InsertBuilder<? extends Builder>> extends QueryBuilder<Builder> {

    Builder into(SqlQueryPart part);
    
    Builder attribute(AttributeQueryPart part);
    
    Builder values(SelectableQueryPart part);
    
    Builder from(SelectableQueryPart part);
}
