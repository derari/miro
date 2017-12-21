package org.cthul.miro.sql;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.syntax.ClauseType;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.db.syntax.StatementBuilder;
import org.cthul.miro.db.syntax.Syntax;
import org.cthul.miro.util.Key;

/**
 *
 * @param <Stmt>
 */
public interface SqlDDL<Stmt> extends RequestType<Stmt>, ClauseType<Stmt> {
    
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
        public Object createDefaultRequest(Syntax syntax, MiConnection cnn) {
            return newStmt(cnn).begin(this);
        }
    }
}
