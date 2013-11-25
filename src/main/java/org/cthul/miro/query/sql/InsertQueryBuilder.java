package org.cthul.miro.query.sql;

import org.cthul.miro.query.adapter.QueryAdapter;
import org.cthul.miro.query.parts.QueryPart;
import org.cthul.miro.query.parts.SelectableQueryPart;

public interface InsertQueryBuilder extends QueryAdapter<InsertQueryBuilder> {

    InsertQueryBuilder into(QueryPart part);
    
    InsertQueryBuilder attribute(String attribute);
    
    InsertQueryBuilder values(SelectableQueryPart part);
    
    InsertQueryBuilder from(SelectableQueryPart part);
}
