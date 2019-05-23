package org.cthul.miro.db.request;

import java.util.function.Consumer;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.syntax.ClauseType;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.db.syntax.Syntax;

/**
 * A query builder.
 */

public interface MiQueryBuilder extends MiQuery, StatementBuilder {

    @Override
    default <Clause> MiQueryBuilder clause(ClauseType<Clause> type, Consumer<? super Clause> code) {
        return (MiQueryBuilder) StatementBuilder.super.clause(type, code);
    }
    
    static final Type TYPE = Type.QUERY;
    
    enum Type implements RequestType<MiQueryBuilder>, ClauseType<MiQueryBuilder> {

        QUERY;

        @Override
        public MiQueryBuilder createDefaultRequest(Syntax syntax, MiConnection cnn) {
            return cnn.newQuery();
        }

        @Override
        public MiQueryBuilder createDefaultClause(Syntax syntax, StatementBuilder stmt) {
            return null;
        }
    }
    
    static MiQueryBuilder create(MiConnection cnn) {
        return cnn.newRequest(TYPE);
    }
}
