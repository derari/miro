package org.cthul.miro.db;

import org.cthul.miro.db.syntax.MiQueryString;
import org.cthul.miro.db.syntax.MiUpdateString;
import org.cthul.miro.db.syntax.RequestType;

/**
 * 
 */
public interface MiConnection {
    
    MiQueryString newQuery();
    
    MiUpdateString newUpdate();
    
    <Stmt> Stmt newStatement(RequestType<Stmt> type);
}
