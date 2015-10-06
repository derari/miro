package org.cthul.miro.db.syntax;

import org.cthul.miro.db.MiConnection;

/**
 *
 */
public interface Syntax {

    default <Req> Req newStatement(MiConnection cnn, RequestType<Req> type) {
        return newStatement(cnn, type, type);
    }
    
    <Req> Req newStatement(MiConnection cnn, RequestType<Req> type, RequestType<Req> onDefault);
    
    default <Cls> Cls newClause(CoreStmtBuilder stmt, ClauseType<Cls> type) {
        return newClause(stmt, type, type);
    }
    
    <Cls> Cls newClause(CoreStmtBuilder stmt, ClauseType<Cls> type, ClauseType<Cls> onDefault);
//    
//    StatementString newStatementString();
}
