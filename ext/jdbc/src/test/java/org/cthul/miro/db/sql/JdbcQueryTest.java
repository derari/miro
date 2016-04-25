package org.cthul.miro.db.sql;

import org.cthul.miro.db.MiException;
import org.cthul.miro.ext.jdbc.JdbcConnection;
import org.cthul.miro.ext.jdbc.JdbcConnection.ConnectionProvider;
import org.cthul.miro.db.stmt.MiDBString;
import org.cthul.miro.db.stmt.MiQueryString;
import org.cthul.miro.db.syntax.AutocloseableBuilder;
import org.cthul.miro.db.syntax.ClauseType;
import org.cthul.miro.db.syntax.Syntax;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 *
 */
public class JdbcQueryTest {

    @Test
    public void test_auto_close() throws MiException {
        ConnectionProvider cp = () -> { throw new ConnectionMock(); };
        JdbcConnection cnn = new JdbcConnection(cp, new MySyntax());
        MiQueryString query = cnn.newQuery();
        query.begin(MY_CLAUSE);
        assertThat(query.toString(), is(""));
        try {
            query.execute();
        } catch (ConnectionMock e) { }
        assertThat(query.toString(), is("ok"));
    }
    
    static final ClauseType<MyClause> MY_CLAUSE = new ClauseType<MyClause>() {
        @Override
        public MyClause createDefaultClause(Syntax syntax, MiDBString dbString, Object owner) {
            return new MyClause(dbString);
        }
    };
    
    static class MyClause implements AutocloseableBuilder {

        final MiDBString dbString;

        public MyClause(MiDBString dbString) {
            this.dbString = dbString;
        }
        
        @Override
        public void close() {
            dbString.append("ok");
        }
    }
    
    static class ConnectionMock extends RuntimeException {
        public ConnectionMock() {
        }
    }
    
    static class MySyntax implements Syntax {        
    }
}
