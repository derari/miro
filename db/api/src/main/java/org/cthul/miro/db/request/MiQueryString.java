package org.cthul.miro.db.request;

import java.util.function.Consumer;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.syntax.ClauseType;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.db.syntax.StatementBuilder;
import org.cthul.miro.db.syntax.Syntax;

/**
 * A query string builder.
 */
public interface MiQueryString extends MiQuery, MiDBString, StatementBuilder {

    @Override
    MiQueryString append(CharSequence chars);

    @Override
    MiQueryString pushArgument(Object argument);

    @Override
    default MiQueryString pushArguments(Iterable<?> args) {
        return (MiQueryString) MiDBString.super.pushArguments(args);
    }

    @Override
    default MiQueryString pushArguments(Object... args) {
        return (MiQueryString) MiDBString.super.pushArguments(args);
    }

    @Override
    default <Clause> MiQueryString clause(ClauseType<Clause> type, Consumer<? super Clause> code) {
        return (MiQueryString) StatementBuilder.super.clause(type, code);
    }
    
    static RequestType<MiQueryString> TYPE = new RequestType<MiQueryString>() {
        @Override
        public MiQueryString createDefaultRequest(Syntax syntax, MiConnection cnn) {
            return cnn.newQuery();
        }
    };
    
    static MiQueryString create(MiConnection cnn) {
        return cnn.newRequest(TYPE);
    }
}
