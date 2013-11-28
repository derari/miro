package org.cthul.miro.query.template;

import org.cthul.miro.query.api.InternalQueryBuilder;
import org.cthul.miro.query.parts.QueryPart;

public interface QueryTemplatePart {
    
    QueryPart addPart(String key, InternalQueryBuilder queryBuilder);
}
