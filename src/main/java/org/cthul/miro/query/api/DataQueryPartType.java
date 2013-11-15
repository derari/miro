package org.cthul.miro.query.api;

public enum DataQueryPartType implements QueryPartType {
    
                // -- for Query Type --
    SELECT,     // SELECT
    TABLE,      // any 
    ATTRIBUTE,  // INSERT
    JOIN,       // any
    SET,        // UPDATE
    VALUES,     // INSERT
    SUBQUERY,   // INSERT
    WHERE,      // S,U,D
    GROUP_BY,   // SELECT
    HAVING,     // SELECT
    ORDER_BY;   // SELECT
}
