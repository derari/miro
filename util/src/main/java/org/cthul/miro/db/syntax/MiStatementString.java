package org.cthul.miro.db.syntax;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiStatement;

/**
 *
 */
public interface MiStatementString extends MiStatement, RequestBuilder<MiStatementString> {
    
    static final Type TYPE = new Type();
    
    static class Type implements RequestType<MiStatementString> {
        @Override
        public MiStatementString newStatement(MiConnection connection) {
            return connection.newStatement();
        }
    }
}
