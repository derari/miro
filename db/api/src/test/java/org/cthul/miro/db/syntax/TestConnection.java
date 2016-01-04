package org.cthul.miro.db.syntax;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.impl.BasicDBStringBuilder;
import org.cthul.miro.db.stmt.MiQueryString;
import org.cthul.miro.db.stmt.MiUpdateString;
import org.cthul.miro.futures.MiAction;

/**
 *
 */
public class TestConnection implements MiConnection {
    
    private final Syntax syntax;
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
    public MiQueryString newQuery() {
        return new Query();
    }

    @Override
    public MiUpdateString newUpdate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <Stmt> Stmt newStatement(RequestType<Stmt> type) {
        return syntax.newStatement(this, type);
    }

    @Override
    public void close() throws MiException {
    }
    
    class Query extends BasicDBStringBuilder implements MiQueryString {

        @Override
        public Query append(CharSequence chars) {
            return (Query) super.append(chars);
        }

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
    }
}
