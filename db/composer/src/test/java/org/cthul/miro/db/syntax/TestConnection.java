package org.cthul.miro.db.syntax;

import java.util.function.Function;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.string.MiDBStringBuilder;
import org.cthul.miro.db.request.MiRequest;
import org.cthul.miro.futures.MiAction;
import org.cthul.miro.db.request.MiQueryBuilder;
import org.cthul.miro.db.request.MiUpdateBuilder;
import org.cthul.miro.db.request.StatementBuilder;
import org.cthul.miro.db.string.MiDBString;

/**
 *
 */
public class TestConnection implements MiConnection {
    
    public final Syntax syntax;
    private String lastQuery = null;

    public TestConnection(Syntax syntax) {
        this.syntax = syntax;
    }

    public TestConnection() {
        this(new TestSyntax());
    }

    public String getLastQuery() {
        return lastQuery;
    }

    @Override
    public MiQueryBuilder newQuery() {
        return new Query();
    }

    @Override
    public MiUpdateBuilder newUpdate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <Req extends MiRequest<?>> Req newRequest(RequestType<Req> type) {
        return syntax.newRequest(this, type);
    }

    @Override
    public void close() throws MiException {
    }
    
    class Query extends MiDBStringBuilder implements MiQueryBuilder {

        @Override
        public MiResultSet execute() throws MiException {
            lastQuery = toString();
            return null;
        }

        @Override
        public MiAction<MiResultSet> asAction() {
            lastQuery = toString();
            return null;
        }

        @Override
        public Query append(CharSequence chars) {
            return (Query) super.append(chars);
        }

        @Override
        public <Clause> Clause begin(ClauseType<Clause> type) {
            if (type == MiDBString.TYPE) return type.cast(this);
            throw new UnsupportedOperationException();
        }

        @Override
        public <Clause> Clause as(Function<StatementBuilder, Clause> factory) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Query pushArgument(Object argument) {
            return (Query) super.pushArgument(argument);
        }

        @Override
        public Query pushArguments(Iterable<?> args) {
            return (Query) super.pushArguments(args);
        }

        @Override
        public Query pushArguments(Object... args) {
            return (Query) super.pushArguments(args);
        }
    }
}
