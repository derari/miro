package org.cthul.miro.query.syntax;

import org.cthul.miro.query.api.QueryType;

public interface QuerySyntax {
    
    QueryStringBuilder newQueryStringBuilder(QueryType queryType);
}
