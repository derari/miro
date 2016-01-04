package org.cthul.miro.map.sql;

import org.cthul.miro.composer.ComposerParts;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.sql.SelectQuery;
import org.cthul.miro.db.sql.syntax.AnsiSqlSyntax;
import org.cthul.miro.db.stmt.MiQueryString;
import org.cthul.miro.db.stmt.MiUpdateString;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.map.impl.MappedStatement;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class MappedSelectStatementTest {
    
    public MappedSelectStatementTest() {
    }
    
    @Before
    public void setUp() {
    }

    @Test
    public void testRequire() {
        SelectQuery qry = new TestStmt()
                .requireOther()
                .whereX(7)
                .requireOther()
                .buildStatement().getStatement();
        assertThat(qry.toString(), 
                is("SELECT \"foo\" "
                + "FROM \"Foo\" \"f\" "
                + "JOIN \"Other\" \"o\" ON \"f\".\"id\" = \"o\".\"id\" "
                + "WHERE X = ?"));
        
    }
    
    static final MappedEntityType<Foo> fooType = new MappedEntityType<>(null, Foo.class);
    
    static class Foo { }
    
    static class TestStmt extends MappedSelectStatement<Foo> {

        public TestStmt() {
            super(fooType, ComposerParts.noOp());
            require(new MiConnection() {
                @Override
                public MiQueryString newQuery() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public MiUpdateString newUpdate() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public <Stmt> Stmt newStatement(RequestType<Stmt> type) {
                    return new AnsiSqlSyntax().newStatement(this, type);
                }
                @Override
                public void close() throws MiException { }
            });
        }

        @Override
        protected void initialize() {
            super.initialize();
            sql().select().id("foo");
            sql().from().namedTable("Foo", "f");
        }

        public TestStmt requireOther() {
            once(sql -> sql.join().namedTable("Other", "o").on().sql("`f`.`id` = `o`.`id`"));
            return this;
        }
        
        public TestStmt whereX(int i) {
            sql().where()
                    .ql("X = ?")
                    .pushArgument(i);
            return this;
        }

        @Override
        public MappedStatement<Foo, SelectQuery> buildStatement() {
            return super.buildStatement();
        }
    }
    
}
