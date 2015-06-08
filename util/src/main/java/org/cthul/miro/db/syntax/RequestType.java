package org.cthul.miro.db.syntax;

import org.cthul.miro.db.MiConnection;

/**
 * Defines database statements.
 * @param <Stmt>
 */
public interface RequestType<Stmt> {
    
    Stmt newStatement(MiConnection connection);
}
