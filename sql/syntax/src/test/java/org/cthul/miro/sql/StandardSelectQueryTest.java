package org.cthul.miro.sql;

import org.cthul.miro.db.MiException;
import org.cthul.miro.sql.impl.StandardSelectQuery;
import org.cthul.miro.sql.syntax.AnsiSqlSyntax;
import org.cthul.miro.db.request.MiQueryString;
import org.junit.Test;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class StandardSelectQueryTest {
    
    SelectQuery sql = new StandardSelectQuery(new AnsiSqlSyntax(), (MiQueryString) null);
    
    @Test
    public void test_select_from() {
        sql.select().ql("foo")
           .from().namedTable("Foo", "f");
        assertThat(sql.toString(), is("SELECT foo FROM \"Foo\" \"f\""));
    }
    
    @Test
    public void test_multi_select() {
        sql.select().ql("foo").and().ql("bar")
            .from().ql("Foo")
            .select().ql("baz");
        assertThat(sql.toString(), is("SELECT foo, bar, baz FROM Foo"));
    }
    
    @Test
    public void test_join() {
        sql.select().ql("foo")
                .from().ql("Foo")
                .join().ql("Bar")
                .leftJoin().ql("Baz").on().ql("f = b");
        assertThat(sql.toString(), is("SELECT foo FROM Foo JOIN Bar LEFT JOIN Baz ON f = b"));
    }
    
    @Test
    public void test_join2() {
        sql.select().ql("foo")
                .from().ql("Foo")
                .join().id("Bar").sql(" b")
                .leftJoin().ql("Baz").on().ql("f = b");
        assertThat(sql.toString(), is("SELECT foo FROM Foo JOIN \"Bar\" b LEFT JOIN Baz ON f = b"));
    }
    
    @Test
    public void test_join_left() {
        sql.select().ql("foo")
                .from().ql("Foo")
                .leftJoin().sql("Baz ON f = b");
        assertThat(sql.toString(), is("SELECT foo FROM Foo LEFT JOIN Baz ON f = b"));
    }
    
    @Test
    public void test_where() {
        sql.select().ql("foo")
                .from().ql("Foo")
                .where().ql("f = ?", 1);
        assertThat(sql.toString(), is("SELECT foo FROM Foo WHERE f = ?"));
    }
    
    @Test
    public void test_where2() {
        sql.select().ql("foo")
                .from().ql("Foo")
                .where().id("f").sql(" = ?", 1);
        assertThat(sql.toString(), is("SELECT foo FROM Foo WHERE \"f\" = ?"));
    }
    
    @Test
    public void test_where3() {
        sql.select().ql("foo")
                .from().ql("Foo")
                .where().sql("`f` = ?", 1)
                .where().sql("`o` = ?", 1);
        assertThat(sql.toString(), is("SELECT foo FROM Foo WHERE \"f\" = ? AND \"o\" = ?"));
    }
    
    @Test
    public void test_doubled_multi() {
        sql.select().select().ql("foo").and().and().ql("bar")
                .from().ql("Foo")
                .join()
                .join().ql("Bar").on()
                .join().ql("Baz").on().and().ql("f = b");
        assertThat(sql.toString(), is("SELECT foo, bar FROM Foo JOIN Bar JOIN Baz ON f = b"));
    }
    
    @Test
    public void test_create() throws MiException {
        SelectQuery qry = SelectQuery.create(new TestConnection());
        qry.select().ql("foo");
        qry.execute();
        assertThat(TestConnection.lastQuery, is("SELECT foo"));
    }
}
