package org.cthul.miro.query.adapter;

import org.cthul.miro.query.QueryType;

public interface QuerySyntax extends DBAdapter {
    
    <Builder extends QueryBuilder<? extends Builder>> QueryString<Builder> newQueryString(QueryType<? super Builder> queryType);
}
