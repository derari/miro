package org.cthul.miro.query.adapter;

import org.cthul.miro.query.api.QueryType;

public interface JdbcAdapter extends DBAdapter {
    
    <Builder extends QueryBuilder<? extends Builder>> JdbcQuery<Builder> newJdbcQuery(QueryType<? super Builder> queryType);   
}
