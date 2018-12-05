package org.cthul.miro.sql.request;

import org.cthul.miro.sql.composer.model.SqlTemplates;
import org.cthul.miro.sql.composer.SelectRequest;
import static org.cthul.matchers.fluent8.FluentAssert.assertThat;
import org.cthul.miro.db.impl.MiDBStringBuilder;
import org.cthul.miro.sql.syntax.AnsiSqlSyntax;
import org.cthul.miro.sql.syntax.SqlSyntax;
import org.junit.Test;
import org.cthul.miro.sql.SqlDQML;

/**
 *
 */
public class SqlTemplatesTest {
    
    SqlSyntax syntax = new AnsiSqlSyntax();
    
    SqlTemplates people = new SqlTemplates("People")
            .attributes("p.`id`, p.first_name AS firstName, p.last_name")
            .from("People p");
    
    SqlTemplates addresses = new SqlTemplates("Addresses")
            .attributes("a.id, a.street, a.city")
            .using("GROUP BY a.id, a.street, a.city")
                .attribute("COUNT(i.id) AS inhabitantCount")
            .from("Addresses a")
            .join("LEFT People i ON a.id = i.address_id");
    
    {
        addresses.join(people.joinedAs("people", "a.id = p.address_id"));
    }
    
    @Test
    public void basic_table() {
        MiDBStringBuilder qryString = new MiDBStringBuilder();
        
        SelectRequest c = people.newSelectComposer();
        
        c.getSelectedAttributes().addAll("id", "firstName");
        c.build(qryString.as(syntax, SqlDQML.select()));
        
        qryString.close();
        assertThat(qryString).hasToString(
                "SELECT p.\"id\", p.first_name AS firstName " +
                "FROM People p");
    }
    
    @Test
    public void join_and_group() {
        MiDBStringBuilder qryString = new MiDBStringBuilder();
        
        SelectRequest c = addresses.newSelectComposer();
        
        c.getSelectedAttributes().addAll("id", "inhabitantCount");
        c.build(qryString.as(syntax, SqlDQML.select()));
        
        qryString.close();
        assertThat(qryString).hasToString(
                "SELECT a.id, COUNT(i.id) AS inhabitantCount " +
                "FROM Addresses a " +
                "LEFT JOIN People i ON a.id = i.address_id " + 
                "GROUP BY a.id, a.street, a.city");
    }
    
    @Test
    public void joined_view() {
        MiDBStringBuilder qryString = new MiDBStringBuilder();
        
        SelectRequest c = addresses.newSelectComposer();
        
        c.getSelectedAttributes().addAll("id", "people.id");
        c.build(qryString.as(syntax, SqlDQML.select()));
        
        qryString.close();
        assertThat(qryString).hasToString(
                "SELECT a.id, p.\"id\" AS \"people.id\" " +
                "FROM Addresses a " +
                "JOIN People p ON a.id = p.address_id");
    }
    
    @Test
    public void attributes_in() {
        MiDBStringBuilder qryString = new MiDBStringBuilder();
        
        SelectRequest c = addresses.newSelectComposer();
        
        c.getSelectedAttributes().addAll("id", "street");
        c.getAttributeFilter().forAttribute("people.firstName").add("John");
        c.build(qryString.as(syntax, SqlDQML.select()));
        
        
        qryString.close();
        assertThat(qryString).hasToString(
                "SELECT a.id, a.street " + 
                "FROM Addresses a " + 
                "JOIN People p ON a.id = p.address_id " + 
                "WHERE p.first_name = ?");
    }
    
    @Test
    public void test_snippets() {
        SqlTemplates instance = new SqlTemplates("Test");
        instance.selectSnippet("f", "SELECT f FROM Foo");
        instance.using("f").selectSnippet("b", (s, a) -> s.where("b = ?", a));
                
        SelectRequest c = instance.newSelectComposer();
        c.getMainView().get("b").batch(1);
        
        MiDBStringBuilder qryString = new MiDBStringBuilder();
        c.build(qryString.as(syntax, SqlDQML.select()));
        qryString.close();
        
        assertThat(qryString).hasToString(
                "SELECT f FROM Foo WHERE b = ?");
        assertThat(qryString.getArguments()).hasSize(1);
        assertThat(qryString.getArguments()).contains(1);
    }
    
    @Test
    public void sql_join() {
        SqlTemplates instance = new SqlTemplates("Test");
        instance.sql("SELECT b.x, f.y, b.z FROM Foo f JOIN Bar b ON f.b = b.id");
                
        SelectRequest c = instance.newSelectComposer();
        c.getSelectedAttributes().add("x");
        
        MiDBStringBuilder qryString = new MiDBStringBuilder();
        c.build(qryString.as(syntax, SqlDQML.select()));
        qryString.close();
        
        assertThat(qryString).hasToString(
                "SELECT b.x FROM Foo f JOIN Bar b ON f.b = b.id");
    }
}
