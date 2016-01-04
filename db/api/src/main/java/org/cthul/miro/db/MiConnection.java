package org.cthul.miro.db;

import org.cthul.miro.db.stmt.MiQueryString;
import org.cthul.miro.db.stmt.MiUpdateString;
import org.cthul.miro.db.syntax.RequestType;

/**
 * Represents a database connection.
 * Can create statements in the database's syntax flavor.
 */
public interface MiConnection extends AutoCloseable {
    
    MiQueryString newQuery();
    
    MiUpdateString newUpdate();
    
    <Stmt> Stmt newStatement(RequestType<Stmt> type);

    @Override
    void close() throws MiException;
}
