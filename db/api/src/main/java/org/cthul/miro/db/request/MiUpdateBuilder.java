package org.cthul.miro.db.request;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.syntax.ClauseType;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.db.syntax.Syntax;

/**
 * An update string builder.
 */
public interface MiUpdateBuilder extends MiUpdate, StatementBuilder {
    
    void addBatch();
    
    static final Type TYPE = Type.UPDATE;
    
    enum Type implements RequestType<MiUpdateBuilder>, ClauseType<MiUpdateBuilder> {

        UPDATE;

        @Override
        public MiUpdateBuilder createDefaultRequest(Syntax syntax, MiConnection cnn) {
            return cnn.newUpdate();
        }

        @Override
        public MiUpdateBuilder createDefaultClause(Syntax syntax, StatementBuilder stmt) {
            return null;
        }
    }
    
    static MiUpdateBuilder create(MiConnection cnn) {
        return cnn.newRequest(TYPE);
    }
}
