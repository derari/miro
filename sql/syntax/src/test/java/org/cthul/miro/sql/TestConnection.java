package org.cthul.miro.sql;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.request.MiQueryBuilder;
import org.cthul.miro.sql.syntax.AnsiSqlSyntax;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.db.syntax.Syntax;
import org.cthul.miro.futures.MiAction;
import org.cthul.miro.db.request.MiRequest;
import org.cthul.miro.db.request.MiUpdateBuilder;
import org.cthul.miro.db.request.StatementBuilder;
import org.cthul.miro.db.string.SyntaxStringBuilder;
import org.cthul.miro.db.syntax.ClauseType;

/**
 *
 * @author C5173086
 */
class TestConnection implements MiConnection {
    
    public static String lastQuery = null;
    
    Syntax syntax = new AnsiSqlSyntax();

    @Override
    public MiQueryBuilder newQuery() {
        class Query extends Req<MiResultSet> implements MiQueryBuilder {
            @Override
            protected <Clause> Clause newNestedClause(StatementBuilder parent, ClauseType<Clause> type) {
                if (type == MiQueryBuilder.TYPE) return type.cast(this);
                return super.newNestedClause(parent, type);
            }
        }
        return new Query();
    }

    @Override
    public MiUpdateBuilder newUpdate() {
        class Update extends Req<Long> implements MiUpdateBuilder {
            @Override
            protected <Clause> Clause newNestedClause(StatementBuilder parent, ClauseType<Clause> type) {
                if (type == MiUpdateBuilder.TYPE) return type.cast(this);
                return super.newNestedClause(parent, type);
            }
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
    
    class Req<R> extends SyntaxStringBuilder implements MiRequest<R>, StatementBuilder {

        public Req() {
            super(syntax);
        }

        @Override
        public R execute() throws MiException {
            close();
            lastQuery = toString();
            return null;
        }

        @Override
        public MiAction<R> asAction() {
            throw new UnsupportedOperationException();
        }
    }
}
