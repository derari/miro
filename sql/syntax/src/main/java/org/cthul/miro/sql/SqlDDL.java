package org.cthul.miro.sql;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.syntax.ClauseType;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.db.syntax.Syntax;
import org.cthul.miro.util.Key;
import org.cthul.miro.db.request.MiRequest;
import org.cthul.miro.db.request.StatementBuilder;

/**
 *
 * @param <Req>
 */
public interface SqlDDL<Req extends MiRequest<?>> extends RequestType<Req>, ClauseType<Req> {
    
    static SqlDDL<CreateStatement> create() { return Type.CREATE_TABLE; }
    
    default Type type() {
        return (Type) this;
    }
    
    public static Type type(Object type) {
        return Key.castDefault(type, Type.NIL);
    }

    public static enum Type implements SqlDDL {

        CREATE_TABLE,
        
        NIL,
        ;
        
        protected StatementBuilder newStmt(MiConnection cnn) {
            return cnn.newUpdate();
        }

        @Override
        public MiRequest<?> createDefaultRequest(Syntax syntax, MiConnection cnn) {
            return (MiRequest) syntax.newClause(newStmt(cnn), this);
        }
    }
}
