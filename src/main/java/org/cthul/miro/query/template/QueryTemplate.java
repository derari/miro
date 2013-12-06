package org.cthul.miro.query.template;

import org.cthul.miro.query.InternalQueryBuilder;
import org.cthul.miro.query.parts.QueryPart;

public interface QueryTemplate {
    
    QueryPart addPart(Object key, InternalQueryBuilder queryBuilder);
}
