package org.cthul.miro.query.jdbc;

import org.cthul.miro.query.api.QueryType;

public interface JdbcAdapter {
    
    JdbcQueryBuilder newJdbcQueryBuilder(QueryType queryType);   
}
