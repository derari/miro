package org.cthul.miro.query.api;

import org.junit.Test;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 */
public class AbstractQueryBuilderTest {
    
    @Test
    public void test_select_query() {
        
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
//        assertThat(sql.getQueryString(), 
//                is("SELECT a AS a, b "
//                + "FROM Data a "
//                + "JOIN Data2 b ON a = ? "
//                + "WHERE a > ? OR a < ? AND b < ? "
//                + "GROUP BY a "
//                + "HAVING a = ? "
//                + "ORDER BY b"));
//        
//        assertThat(sql.getArguments(), 
//                contains((Object) "J1", "W1", "W2", "W3", "H1"));
//        
//    }
//    
}