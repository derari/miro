package org.cthul.miro.db.syntax;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.stmt.MiDBString;

/**
 * Defines a query language.
 */
public interface Syntax {

    default <Req> Req newStatement(MiConnection cnn, RequestType<Req> type) {
        return newStatement(cnn, type, type);
    }
    
    default <Req> Req newStatement(MiConnection cnn, RequestType<Req> type, RequestType<Req> onDefault) {
        return onDefault.createDefaultRequest(this, cnn);
    }
    
    default <Cls> Cls newClause(MiDBString stmt, ClauseType<Cls> type) {
        return newClause(stmt, null, type);
    }
    
    default <Cls> Cls newClause(MiDBString stmt, Object owner, ClauseType<Cls> type) {
        return newClause(stmt, owner, type, type);
    }
    
    default <Cls> Cls newClause(MiDBString stmt, Object owner, ClauseType<Cls> type, ClauseType<Cls> onDefault) {
        return onDefault.createDefaultClause(this, stmt, owner);
    }
    
    default void appendConstanct(Object key, QlBuilder<?> query) {
        throw new IllegalArgumentException(String.valueOf(key));
    }
    
    default QlCode getConstanct(Object key) {
        class Constant implements QlCode {
            @Override
            public void appendTo(QlBuilder<?> qlBuilder) {
                appendConstanct(key, qlBuilder);
            }
            @Override
            public String toString() {
                return String.valueOf(key);
            }
        }
        return new Constant();
    }
}
