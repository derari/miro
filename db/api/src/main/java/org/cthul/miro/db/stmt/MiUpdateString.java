package org.cthul.miro.db.stmt;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.db.syntax.StatementBuilder;
import org.cthul.miro.db.syntax.Syntax;

/**
 * An update string builder.
 */
public interface MiUpdateString extends MiUpdate, MiDBString, StatementBuilder {
    
    void addBatch();
    
    static RequestType<MiUpdateString> TYPE = new RequestType<MiUpdateString>() {
        @Override
        public MiUpdateString createDefaultRequest(Syntax syntax, MiConnection cnn) {
            return cnn.newUpdate();
        }
    };
    
    static MiUpdateString create(MiConnection cnn) {
        return cnn.newStatement(TYPE);
    }
}
