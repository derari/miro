package org.cthul.miro.query.sql;

import org.cthul.miro.query.adapter.QueryBuilder;
import org.cthul.miro.query.QueryPartType;
import org.cthul.miro.query.parts.QueryPart;

public enum DataQueryPart implements QueryPartType {
        
                // for query type
    SELECT,     // SELECT
    ATTRIBUTE,  // S->SELECT,U,I
    TABLE,      // S,U,I,D
    JOIN,       // S,U,I,D
    SET,        // UPDATE
    VALUES,     // U(Values),I(Selectable),D(Values)
    SUBQUERY,   // S->TABLE,I(Selectable)
    WHERE,      // S,U,D
    FILTER_ATTRIBUTE,  // U,D
    GROUP_BY,   // SELECT
    HAVING,     // SELECT
    ORDER_BY,   // SELECT
    UNKNOWN;

    @Override
    public void addPartTo(QueryPart part, QueryBuilder query) {
        query.add(this, part);
    }

    public static DataQueryPart get(QueryPartType type) {
        if (type instanceof DataQueryPart) {
            return (DataQueryPart) type;
        }
        return UNKNOWN;
    }
}
