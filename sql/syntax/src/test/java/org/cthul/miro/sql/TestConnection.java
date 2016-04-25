package org.cthul.miro.sql;

import javax.management.Query;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.impl.MiDBStringBuilder;
import org.cthul.miro.db.impl.MiDBStringDelegator;
import org.cthul.miro.sql.syntax.AnsiSqlSyntax;
import org.cthul.miro.db.stmt.MiDBString;
import org.cthul.miro.db.stmt.MiQueryString;
import org.cthul.miro.db.stmt.MiStatement;
import org.cthul.miro.db.stmt.MiUpdateString;
import org.cthul.miro.db.syntax.ClauseType;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.db.syntax.StatementBuilder;
import org.cthul.miro.db.syntax.Syntax;
import org.cthul.miro.futures.MiAction;

/**
 *
 * @author C5173086
 */
class TestConnection implements MiConnection {
    
    public static String lastQuery = null;
    
    Syntax syntax = new AnsiSqlSyntax();

    @Override
    public MiQueryString newQuery() {
        class Query extends Stmt<MiResultSet, Query> implements MiQueryString {
        }
        return new Query();
    }

    @Override
    public MiUpdateString newUpdate() {
        class Update extends Stmt<Long, Update> implements MiUpdateString {
            @Override
            public void addBatch() { }
        }
        return new Update();
    }

    @Override
    public <Stmt> Stmt newStatement(RequestType<Stmt> type) {
        return syntax.newStatement(this, type);
    }

    @Override
    public void close() throws MiException {
    }
    
    class Stmt<R, This extends Stmt<R, This>> extends MiDBStringDelegator<This> implements MiStatement<R>, StatementBuilder {
        MiDBStringBuilder string = new MiDBStringBuilder();
        @Override
        protected MiDBString getDelegatee() {
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
