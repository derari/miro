package org.cthul.miro.query;

import org.junit.Test;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 */
public class QueryWithTemplateTest {
    
    private QueryTemplate template = new QueryTemplate(){{
        select("a.col1 AS a1, b.col1 AS b1");
        optional_select("a.col2 AS a2, c.col1 AS c1");
        internal_select("c.col2 AS c2");
        from("Table1 a");
        using("a")
            .join("Table2 b ON a.key = b.key")
            .join("Table3 c ON a.key = c.key");
        always()
            .join("Table4 d ON a.key = d.key");
        using("c2")
            .join("Table5 e ON c.key = e.key")
            .where("c1 =", "c.col1 = ?");
        groupBy("byA1", "a.col1");
        groupBy("byE1", "e.col1");
    }};
    
    @Test
    public void test_select_all() {
        QueryWithTemplate qry = new QueryWithTemplate(template);
        qry.select("*");
        assertThat(qry.getQueryString(),
                is("SELECT a.col1 AS a1, b.col1 AS b1 "
                + "FROM Table1 a "
                + "JOIN Table4 d ON a.key = d.key JOIN Table2 b ON a.key = b.key"));
    }
    
    @Test
    public void test_auto_dependency() {
        QueryWithTemplate qry = new QueryWithTemplate(template);
        qry.select("c1");
        assertThat(qry.getQueryString(),
                is("SELECT c.col1 AS c1 "
                + "FROM Table1 a "
                + "JOIN Table4 d ON a.key = d.key JOIN Table3 c ON a.key = c.key"));
    }
    
    @Test
    public void test_where() {
        QueryWithTemplate qry = new QueryWithTemplate(template);
        qry.select("a1");
        qry.where("c1 =", 1);
        assertThat(qry.getQueryString(),
                is("SELECT a.col1 AS a1, c.col2 AS c2 "
                + "FROM Table1 a "
                + "JOIN Table4 d ON a.key = d.key JOIN Table3 c ON a.key = c.key "
                + "WHERE c.col1 = ?"));
        assertThat(qry.getArguments(), 
                contains((Object) 1));
    }
}