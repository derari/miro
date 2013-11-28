package org.cthul.miro.query.parts;

import java.util.List;

public interface QueryPart {
    
    String getKey();
    
    void put(String key, Object... args);
    
    void appendSqlTo(StringBuilder sqlBuilder);

    void appendArgsTo(List<Object> args);
}
