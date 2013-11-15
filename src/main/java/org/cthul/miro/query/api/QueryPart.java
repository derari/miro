package org.cthul.miro.query.api;

import java.util.List;

public interface QueryPart {
    
    QueryPartType getPartType();

    void put(String key, Object[] args);

    void appendSqlTo(StringBuilder sqlBuilder);

    void appendArgsTo(List<Object> args);
}
