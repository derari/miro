package org.cthul.miro.query.parts;

import java.util.List;

public interface SqlQueryPart extends QueryPart {
    
    void appendSqlTo(StringBuilder sqlBuilder);

    void appendArgsTo(List<Object> args);
}
