package org.cthul.miro.sql;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.impl.MiDBStringBuilder;
import org.cthul.miro.db.impl.MiDBStringDelegator;
import org.cthul.miro.sql.syntax.AnsiSqlSyntax;
import org.cthul.miro.db.request.MiDBString;
import org.cthul.miro.db.request.MiQueryString;
import org.cthul.miro.db.request.MiUpdateString;
import org.cthul.miro.db.syntax.ClauseType;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.db.syntax.StatementBuilder;
import org.cthul.miro.db.syntax.Syntax;
import org.cthul.miro.futures.MiAction;
import org.cthul.miro.db.request.MiRequest;

/**
 *
 * @author C5173086
 */
class TestConnection implements MiConnection {
    
    public static String lastQuery = null;
    
    Syntax syntax = new AnsiSqlSyntax();

    @Override
    public MiQueryString newQuery() {
        class Query extends Req<MiResultSet, Query> implements MiQueryString {
        }
        return new Query();
    }

    @Override
    public MiUpdateString newUpdate() {
        class Update extends Req<Long, Update> implements MiUpdateString {
            @Override
            public void addBatch() { }
        }
        return new Update();
    }

    @Override
    public <Req extends MiRequest<?>> Req newRequest(RequestType<Req> type) {
        return syntax.newRequest(this, type);
    }

    @Override
    public void close() throws MiException {
    }
    
    class Req<R, This extends Req<R, This>> extends MiDBStringDelegator<This> implements MiRequest<R>, StatementBuilder {
        MiDBStringBuilder string = new MiDBStringBuilder();
        @Override
        protected MiDBString getDelegate() {
            return string;
        }

        @Override
        public <Clause> Clause begin(ClauseType<Clause> type) {
            return newNestedClause((str) -> syntax.newClause(str, this, type));
        }

        @Override
        public R execute() throws MiException {
            close();
            lastQuery = string.toString();
            return null;
        }

        @Override
        public MiAction<R> asAction() {
            throw new UnsupportedOperationException();
        }
    }
}
