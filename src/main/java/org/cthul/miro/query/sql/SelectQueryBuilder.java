package org.cthul.miro.query.sql;

import org.cthul.miro.query.adapter.QueryAdapter;
import org.cthul.miro.query.parts.QueryPart;

public interface SelectQueryBuilder extends QueryAdapter<SelectQueryBuilder> {
    
    SelectQueryBuilder select(QueryPart part);
    
    SelectQueryBuilder from(QueryPart part);
    
    SelectQueryBuilder join(QueryPart part);
    
    SelectQueryBuilder where(QueryPart part);
    
    SelectQueryBuilder groupBy(QueryPart part);
    
    SelectQueryBuilder having(QueryPart part);
    
    SelectQueryBuilder orderBy(QueryPart part);
}
