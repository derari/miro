package org.cthul.miro.sql.template;

import static org.cthul.matchers.fluent8.FluentAssert.assertThat;
import org.cthul.miro.request.impl.SimpleRequestComposer;
import org.cthul.miro.request.template.TemplateLayer;
import org.cthul.miro.db.impl.MiDBStringBuilder;
import org.cthul.miro.request.RequestComposer;
import org.cthul.miro.sql.SelectBuilder;
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
        
        TemplateLayer<SelectBuilder> tmpl = people.getSelectLayer();
        RequestComposer<SelectBuilder> c = new SimpleRequestComposer<>(tmpl.build());
        
        c.node(SqlComposerKey.ATTRIBUTES).addAll("id", "firstName");
        c.build(qryString.as(syntax, SqlDQML.select()));
        
        qryString.close();
        assertThat(qryString).hasToString(
                "SELECT p.\"id\", p.first_name AS firstName " +
                "FROM People p");
    }
    
    @Test
    public void join_and_group() {
        MiDBStringBuilder qryString = new MiDBStringBuilder();
        
        TemplateLayer<SelectBuilder> tmpl = addresses.getSelectLayer();
        RequestComposer<SelectBuilder> c = new SimpleRequestComposer<>(tmpl.build());
        
        c.node(SqlComposerKey.ATTRIBUTES).addAll("id", "inhabitantCount");
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
        
        TemplateLayer<SelectBuilder> tmpl = addresses.getSelectLayer();
        RequestComposer<SelectBuilder> c = new SimpleRequestComposer<>(tmpl.build());
        
        c.node(SqlComposerKey.ATTRIBUTES).addAll("id", "people.id");
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
        
        TemplateLayer<SelectBuilder> tmpl = addresses.getSelectLayer();
        RequestComposer<SelectBuilder> c = new SimpleRequestComposer<>(tmpl.build());
        
        c.node(SqlComposerKey.ATTRIBUTES).addAll("id", "street");
        c.node(SqlComposerKey.ATTRIBUTE_FILTER)
                .forAttributes("people.firstName").add(new Object[]{"John"});
        c.build(qryString.as(syntax, SqlDQML.select()));
        
        
        qryString.close();
        assertThat(qryString).hasToString(
                "SELECT a.id, a.street " + 
                "FROM Addresses a " + 
                "JOIN People p ON a.id = p.address_id " + 
                "WHERE p.first_name = ?");
    }
}
