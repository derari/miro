package org.cthul.miro.sql;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.syntax.ClauseType;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.db.syntax.Syntax;
import org.cthul.miro.util.Key;
import org.cthul.miro.db.request.MiRequest;
import org.cthul.miro.db.request.StatementBuilder;

/**
 * Defines the four request types of the SQL Data Query and Manipulation Language:
 * SELECT, INSERT, UPDATE, and DELETE.
 * @param <Req>
 */
public interface SqlDQML<Req extends MiRequest<?>> extends RequestType<Req>, ClauseType<Req> {
    
    static SqlDQML<SelectQuery> select() { return Type.SELECT; }
    static SqlDQML<InsertStatement> insert() { return Type.INSERT; }
//    static SqlDQML<Void> update() { return Type.UPDATE; }
//    static SqlDQML<Void> delete() { return Type.DELETE; }
    
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
        public MiRequest<?> createDefaultRequest(Syntax syntax, MiConnection cnn) {
            return (MiRequest) syntax.newClause(newStmt(cnn), this);
        }
    }
}
