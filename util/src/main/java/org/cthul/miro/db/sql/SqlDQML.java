package org.cthul.miro.db.sql;

import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.util.Key;

/**
 *
 * @param <Stmt>
 */
public interface SqlDQML<Stmt> extends RequestType<Stmt> {
    
    default DQML type() {
        return (DQML) this;
    }
    
    public static final SqlDQML<SelectQuery> SELECT = DQML.SELECT;
    
    public static DQML type(RequestType<?> type) {
        return Key.castDefault(type, DQML.class, DQML.NIL);
    }

    @SuppressWarnings("LocalVariableHidesMemberVariable")
    public static enum DQML implements SqlDQML {

        SELECT,
        
        NIL,
        ;
    }
}
