package org.cthul.miro.db.syntax;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.request.MiDBString;
import org.cthul.miro.db.request.MiRequest;

/**
 * Defines a query language.
 */
public interface Syntax {

    default <Req extends MiRequest<?>> Req newRequest(MiConnection cnn, RequestType<Req> type) {
        return newRequest(cnn, type, type);
    }
    
    default <Req extends MiRequest<?>> Req newRequest(MiConnection cnn, RequestType<Req> type, RequestType<Req> onDefault) {
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
    
    default void appendConstant(Object key, QlBuilder<?> query) {
        throw new IllegalArgumentException(String.valueOf(key));
    }
    
    default QlCode getConstant(Object key) {
        class Constant implements QlCode {
            @Override
            public void appendTo(QlBuilder<?> qlBuilder) {
                appendConstant(key, qlBuilder);
            }
            @Override
            public String toString() {
                return String.valueOf(key);
            }
        }
        return new Constant();
    }
}
