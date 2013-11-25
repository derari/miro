package org.cthul.miro.query.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.cthul.miro.query.api.*;
import org.cthul.miro.query.jdbc.JdbcQueryBuilder;
import org.cthul.miro.query.syntax.QueryStringBuilder;
import org.cthul.miro.test.TestDB;
import org.cthul.miro.test.TestQueryPart;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 */
public class AnsiSqlTest {
    
    @Before
    public void setUp() {
        TestDB.clear();
        TestDB.insertAddress(0, "Street 1", "City1");
    }
    
    @Test
    public void test_select_sql() {
        QueryStringBuilder sql = new AnsiSql().newQueryStringBuilder(DataQueryType.SELECT);
        addParts(sql,
                new TestQueryPart(DataQueryPartType.WHERE, "a > ? OR a < ?", "W1", "W2"),
                new TestQueryPart.Attribute("a AS a"),
                new TestQueryPart(DataQueryPartType.ORDER_BY, "b"),
                new TestQueryPart(DataQueryPartType.TABLE, "Data a"),
                new TestQueryPart(DataQueryPartType.JOIN, "JOIN Data2 b ON a = ?", "J1"),
                new TestQueryPart(DataQueryPartType.SELECT, "b"),
                new TestQueryPart(DataQueryPartType.HAVING, "a = ?", "H1"),
                new TestQueryPart(DataQueryPartType.WHERE, "b < ?", "W3"),
                new TestQueryPart(DataQueryPartType.GROUP_BY, "a"));
        assertThat(sql.getQueryString(), 
                is("SELECT a AS a, b "
                + "FROM Data a "
                + "JOIN Data2 b ON a = ? "
                + "WHERE a > ? OR a < ? AND b < ? "
                + "GROUP BY a "
                + "HAVING a = ? "
                + "ORDER BY b"));
        
        assertThat(sql.getArguments(0), 
                contains((Object) "J1", "W1", "W2", "W3", "H1"));
    }
    
    @Test
    public void test_select_jdbc() throws SQLException {
        JdbcQueryBuilder qry = new AnsiSql().newJdbcQueryBuilder(DataQueryType.SELECT);
        addParts(qry,
                new TestQueryPart(DataQueryPartType.SELECT, "street"),
                new TestQueryPart(DataQueryPartType.TABLE, "Addresses"));
        ResultSet rs = qry.execute(TestDB.getConnection());
        rs.next();
        assertThat(rs.getString(1), is("Street 1"));
    }
    
    @Test
    public void test_insert_values_sql() {
        QueryStringBuilder sql = new AnsiSql().newQueryStringBuilder(DataQueryType.INSERT);
        addParts(sql,
                new TestQueryPart.Attribute("a"),
                new TestQueryPart.Attribute("b"),
                new TestQueryPart(DataQueryPartType.TABLE, "Data"),
                new TestQueryPart.Values("1", "2"),
                new TestQueryPart.Values("3", "4"));
        assertThat(sql.getQueryString(), 
                is("INSERT INTO Data(a,b) "
                + "VALUES (?,?), (?,?)"));
        
        assertThat(sql.getArguments(0), 
                contains((Object) "1", "2", "3", "4"));
    }
    
    @Test
    public void test_insert_values_jdbc() throws SQLException {
        JdbcQueryBuilder qry = new AnsiSql().newJdbcQueryBuilder(DataQueryType.INSERT);
        addParts(qry,
                new TestQueryPart.Attribute("street"),
                new TestQueryPart.Attribute("city"),
                new TestQueryPart(DataQueryPartType.TABLE, "Addresses"),
                new TestQueryPart.Values("Street A", "City X"),
                new TestQueryPart.Values("Street B", "City X"));
        
        ResultSet rs = qry.execute(TestDB.getConnection());
        rs.next();
        int i1 = rs.getInt("id");
        rs.next();
        int i2 = rs.getInt("id");
        assertThat(i1, is(greaterThan(0)));
        assertThat(i2, is(greaterThan(i1)));
    }
    
    private void addParts(QueryAdapter adapter, QueryPart... parts) {
        for (QueryPart qp: parts) {
            adapter.addPart(qp);
        }
    }
    
//    @Test
//    public void test_all() {
//        SqlQueryBuilder sql = new SqlQueryBuilder();
//        
//        sql.select("a AS a, b")
//                .where("a > ? OR a < ?", "W1", "W2")
//                .orderBy("b")
//                .from("Data a")
//                .join("Data2 b ON a = ?", "J1")
//                .having("a = ?", "H1")
//                .where("b < ?", "W3")
//                .groupBy("a");
//        
//        
//    }
//    
}