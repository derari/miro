package org.cthul.miro.query.adapter;

import org.cthul.miro.query.api.QueryType;

public interface JdbcAdapter {
    
    <Builder> JdbcQuery<Builder> newJdbcQuery(QueryType<Builder> queryType);   
}
