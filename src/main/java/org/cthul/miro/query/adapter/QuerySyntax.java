package org.cthul.miro.query.adapter;

import org.cthul.miro.query.api.QueryType;

public interface QuerySyntax {
    
    <Builder> QueryString<Builder> newQueryString(QueryType<Builder> queryType);
}
