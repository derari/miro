package org.cthul.miro.query.template;

import org.cthul.miro.test.*;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

/**
 *
 */
public class DataQueryTemplateProviderTest {
    
    private DataQueryTemplateProvider template = new DataQueryTemplateProvider() {{
        generatedKeys("a.'id'");
        attributes("a.'street' AS 'Street', a.'city' AS 'City'");
        select("foo() AS `foo`");
        table("'Addresses' a");
        join("'People' p ON a.id = p.addressId");
    }};
    
    @Test
    public void test_select_all() {
        TestSelectQuery qry = new TestSelectQuery(template);
        qry.select("*");
        assertThat(qry.getQueryString(),
                is("SELECT a.'id' AS 'id', a.'street' AS 'Street', "
                +         "a.'city' AS 'City', foo() AS `foo` "
                + "FROM 'Addresses' a"));
    }
    
    @Test
    public void test_insert() {
        TestInsertQuery qry = new TestInsertQuery(template);
        qry.insert("Street", "City")
                .tuple("street 1", "city1")
                .tuple("street 2", "city2");
        assertThat(qry.getQueryString(),
                is("INSERT INTO 'Addresses'('street','city') "
                + "VALUES (?,?), (?,?)"));
    }
    
    @Test
    public void test_update() {
        TestUpdateQuery qry = new TestUpdateQuery(template);
        qry.update("Street", "City")
                .tuple(1, "street2", "city2")
                .tuple(2, "street3", "city2");
        assertThat(qry.getQueryString(),
                is("UPDATE 'Addresses' a "
                + "SET 'street' = ?, 'city' = ? WHERE 'id' = ?"));
    }
}