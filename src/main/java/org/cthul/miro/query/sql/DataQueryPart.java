package org.cthul.miro.query.sql;

import org.cthul.miro.query.adapter.QueryBuilder;
import org.cthul.miro.query.api.QueryPartType;
import org.cthul.miro.query.parts.QueryPart;

public enum DataQueryPart implements QueryPartType {
        
                // for query type
    SELECT,     // SELECT
    ATTRIBUTE,  // S->SELECT,U->SET,I
    TABLE,      // S,U,I,D
    JOIN,       // S,U,I,D
    SET,        // UPDATE
    VALUES,     // U(Values), I(Selectable)
    SUBQUERY,   // S->TABLE,I->FROM
    WHERE,      // S,U,D
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
