package org.cthul.miro.db;

import org.cthul.miro.db.syntax.MiQueryString;
import org.cthul.miro.db.syntax.MiStatementString;
import org.cthul.miro.db.syntax.RequestType;

/**
 * 
 */
public interface MiConnection {
    
    MiQueryString newQuery();
//    
//    default <Query> Query newQuery(RequestType<Query, ? super MiQueryString> type) {
//        return type.newStatement(newQuery());
//    }
    
    MiStatementString newStatement();
//    
//    default <Stmt> Stmt newStatement(RequestType<Stmt, ? super MiStatementString> type) {
//        return type.newStatement(newStatement());
//    }
}
