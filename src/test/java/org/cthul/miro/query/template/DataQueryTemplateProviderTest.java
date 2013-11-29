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
        generatedKeys("a.'id' AS 'Id'");
        attributes("a.'street' AS 'Street', a.'city' AS 'City'");
        select("foo() AS `foo`");
        using("p.id", "groupBy-keys")
                .optionalSelect("COUNT(p.'id') AS 'Inhabitants'");
        table("'Addresses' a");
        join("'People' p ON a.'id' = p.'addressId'");
    }};
    
    @Test
    public void test_select_all() {
        TestSelectQuery qry = new TestSelectQuery(template);
        qry.select("*");
        assertThat(qry.getQueryString(),
                is("SELECT a.'id' AS 'Id', a.'street' AS 'Street', "
                +         "a.'city' AS 'City', foo() AS `foo` "
                + "FROM 'Addresses' a"));
    }
    
    @Test
    public void test_select_auto_dependencies() {
        TestSelectQuery qry = new TestSelectQuery(template);
        qry.select("Id, Inhabitants");
        assertThat(qry.getQueryString(),
                is("SELECT a.'id' AS 'Id', COUNT(p.'id') AS 'Inhabitants' "
                + "FROM 'Addresses' a "
                + "JOIN 'People' p ON a.'id' = p.'addressId' "
                + "GROUP BY a.'id'"));
    }
    
    @Test
    public void test_select_orderBy() {
        TestSelectQuery qry = new TestSelectQuery(template);
        qry.select("Street")
                .orderBy("Inhabitants DESC");
        assertThat(qry.getQueryString(),
                is("SELECT a.'street' AS 'Street' "
                + "FROM 'Addresses' a "
                + "JOIN 'People' p ON a.'id' = p.'addressId' "
                + "GROUP BY a.'id' "
                + "ORDER BY COUNT(p.'id') DESC"));
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
    
    @Test
    public void test_delete() {
        TestDeleteQuery qry = new TestDeleteQuery(template);
        qry
                .tuple(1)
                .tuple(2);
        assertThat(qry.getQueryString(),
                is("DELETE FROM 'Addresses' a "
                + "WHERE 'id' = ?"));
    }
}