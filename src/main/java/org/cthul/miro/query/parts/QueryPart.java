package org.cthul.miro.query.parts;

import java.util.List;

public interface QueryPart {
    
    void appendSqlTo(StringBuilder sqlBuilder);

    void appendArgsTo(List<Object> args);
}
