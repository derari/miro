package org.cthul.miro.query.sql;

import org.cthul.miro.query.adapter.QueryBuilder;
import org.cthul.miro.query.parts.AttributeQueryPart;
import org.cthul.miro.query.parts.QueryPart;
import org.cthul.miro.query.parts.SelectableQueryPart;

public interface InsertBuilder<Builder extends InsertBuilder<? extends Builder>> extends QueryBuilder<Builder> {

    Builder into(QueryPart part);
    
    Builder attribute(AttributeQueryPart part);
    
    Builder values(SelectableQueryPart part);
    
    Builder from(SelectableQueryPart part);
}
