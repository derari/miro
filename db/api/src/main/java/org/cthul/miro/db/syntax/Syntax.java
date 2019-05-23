package org.cthul.miro.db.syntax;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.request.MiRequest;
import org.cthul.miro.db.request.StatementBuilder;

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
    
    default <Cls> Cls newClause(StatementBuilder stmt, ClauseType<Cls> type) {
        return newClause(stmt, type, type);
    }
    
    default <Cls> Cls newClause(StatementBuilder stmt, ClauseType<Cls> type, ClauseType<Cls> onDefault) {
        return onDefault.createDefaultClause(this, stmt);
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
