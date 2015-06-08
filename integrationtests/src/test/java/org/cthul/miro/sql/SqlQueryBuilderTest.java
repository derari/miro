package org.cthul.miro.sql;

import org.cthul.miro.db.sql.SqlQueryBuilder;
import org.cthul.miro.db.sql.AnsiSqlRequest;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 *
 */
public class SqlQueryBuilderTest {
    
    SqlQueryBuilder sql = new SqlQueryBuilder(null, AnsiSqlRequest::new);

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
    public void test_doubled_multi() {
        sql.select().select().ql("foo").and().and().ql("bar")
                .from().ql("Foo")
                .join()
                .join().ql("Bar").on()
                .join().ql("Baz").on().and().ql("f = b");
        assertThat(sql.toString(), is("SELECT foo, bar FROM Foo JOIN Bar JOIN Baz ON f = b"));
    }
}
