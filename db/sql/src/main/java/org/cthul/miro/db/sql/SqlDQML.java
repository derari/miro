package org.cthul.miro.db.sql;

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
public interface SqlDQML<Stmt> extends RequestType<Stmt>, ClauseType<Stmt> {
    
    static SqlDQML<SelectQuery> select() { return Type.SELECT; }
    static SqlDQML<InsertStatement> insert() { return Type.INSERT; }
    
    default Type type() {
        return (Type) this;
    }
    
    public static Type type(Object type) {
        return Key.castDefault(type, Type.NIL);
    }

    @SuppressWarnings("LocalVariableHidesMemberVariable")
    public static enum Type implements SqlDQML {

        SELECT {
            @Override
            protected StatementBuilder newStmt(MiConnection cnn) {
                return cnn.newQuery();
            }
        },
        INSERT,
        UPDATE,
        DELETE,
        
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
