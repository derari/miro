package org.cthul.miro.db.syntax;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.stmt.MiDBString;

/**
 * Defines a query language.
 */
public interface Syntax {

    default <Req> Req newStatement(MiConnection cnn, RequestType<Req> type) {
        return newStatement(cnn, type, type);
    }
    
    <Req> Req newStatement(MiConnection cnn, RequestType<Req> type, RequestType<Req> onDefault);
    
    default <Cls> Cls newClause(MiDBString stmt, ClauseType<Cls> type) {
        return newClause(stmt, null, type);
    }
    
    default <Cls> Cls newClause(MiDBString stmt, Object owner, ClauseType<Cls> type) {
        return newClause(stmt, owner, type, type);
    }
    
    <Cls> Cls newClause(MiDBString stmt, Object owner, ClauseType<Cls> type, ClauseType<Cls> onDefault);

}
