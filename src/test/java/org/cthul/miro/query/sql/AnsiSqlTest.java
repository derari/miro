package org.cthul.miro.query.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.cthul.miro.query.adapter.JdbcQuery;
import org.cthul.miro.query.adapter.QueryString;
import org.cthul.miro.test.TestDB;
import org.cthul.miro.test.TestQueryPart;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class AnsiSqlTest {
    
    @Before
    public void setUp() {
        TestDB.clear();
        TestDB.insertAddress(0, "Street 1", "City1");
    }
    
    @Test
    public void test_select_sql() {
        QueryString<SelectQueryBuilder> sql = new AnsiSql().newQueryString(DataQuery.SELECT);
        
        sql.getBuilder()
                .where(new TestQueryPart("a > ? OR a < ?", "W1", "W2"))
                .select(new TestQueryPart("a AS a"))
                .orderBy(new TestQueryPart("b"))
                .from(new TestQueryPart("Data a"))
                .join(new TestQueryPart("JOIN Data2 b ON a = ?", "J1"))
                .select(new TestQueryPart("b"))
                .having(new TestQueryPart("a = ?", "H1"))
                .where(new TestQueryPart("b < ?", "W3"))
                .groupBy(new TestQueryPart("a"));
        
        
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
        JdbcQuery<SelectQueryBuilder> qry = new AnsiSql().newJdbcQuery(DataQuery.SELECT);
        
        qry.getBuilder()
                .select(new TestQueryPart("street"))
                .from(new TestQueryPart("Addresses"));
        
        ResultSet rs = qry.execute(TestDB.getConnection());
        rs.next();
        assertThat(rs.getString(1), is("Street 1"));
    }
    
    @Test
    public void test_insert_values_sql() {
        QueryString<InsertQueryBuilder> sql = new AnsiSql().newQueryString(DataQuery.INSERT);
        sql.getBuilder()
                .attribute("a")
                .into(new TestQueryPart("Data"))
                .attribute("b")
                .values(new TestQueryPart.Values("1", "2"))
                .values(new TestQueryPart.Values("3", "4"));
        assertThat(sql.getQueryString(), 
                is("INSERT INTO Data(a,b) "
                + "VALUES (?,?), (?,?)"));
        
        assertThat(sql.getArguments(0), 
                contains((Object) "1", "2", "3", "4"));
    }
    
    @Test
    public void test_insert_values_jdbc() throws SQLException {
        JdbcQuery<InsertQueryBuilder> qry = new AnsiSql().newJdbcQuery(DataQuery.INSERT);
        qry.getBuilder()
                .attribute("street")
                .attribute("city")
                .into(new TestQueryPart("Addresses"))
                .values(new TestQueryPart.Values("Street A", "City2"))
                .values(new TestQueryPart.Values("Street B", "City2"));
        
        ResultSet rs = qry.execute(TestDB.getConnection());
        rs.next();
        int i1 = rs.getInt("id");
        rs.next();
        int i2 = rs.getInt("id");
        assertThat(i1, is(greaterThan(0)));
        assertThat(i2, is(greaterThan(i1)));
    }

    @Test
    public void test_update_set_sql() {
        QueryString<UpdateQueryBuilder> sql = new AnsiSql().newQueryString(DataQuery.UPDATE);
        sql.getBuilder()
                .set(new TestQueryPart("a = ?", "1"))
                .set("b")
                .update(new TestQueryPart("Data"))
                .where(new TestQueryPart("id = ?", "2"))
                .where("id2")
                .values(new TestQueryPart.Values(1, new Object[]{"X", "Y"}));
        
        assertThat(sql.getQueryString(), 
                is("UPDATE Data "
                + "SET b = ?, a = ? "
                + "WHERE id2 = ? AND id = ?"));
        
        assertThat(sql.getArguments(0), 
                contains((Object) "X", "1", "Y", "2"));
    }
}