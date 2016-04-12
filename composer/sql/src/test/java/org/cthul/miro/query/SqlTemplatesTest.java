package org.cthul.miro.query;

import static org.cthul.matchers.fluent8.FluentAssert.assertThat;
import org.cthul.miro.composer.RequestComposer;
import org.cthul.miro.composer.impl.SimpleRequestComposer;
import org.cthul.miro.composer.sql.template.SqlTemplates;
import org.cthul.miro.composer.template.TemplateLayer;
import org.cthul.miro.db.impl.MiDBStringBuilder;
import org.cthul.miro.db.sql.SelectBuilder;
import org.cthul.miro.db.sql.syntax.AnsiSqlSyntax;
import org.cthul.miro.db.sql.syntax.SqlSyntax;
import org.junit.Test;
import org.cthul.miro.db.sql.SqlDQML;

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
        addresses.join(people.joinAs("people", "a.id = p.address_id"));
    }
    
    @Test
    public void basic_table() {
        MiDBStringBuilder qryString = new MiDBStringBuilder();
        
        TemplateLayer<SelectBuilder> tmpl = people.getSelectLayer();
        RequestComposer<SelectBuilder> c = new SimpleRequestComposer<>(tmpl.build());
        
        c.requireAll("id", "firstName");
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
        
        c.requireAll("id", "inhabitantCount");
        c.build(qryString.as(syntax, SqlDQML.select()));
        
        qryString.close();
        assertThat(qryString).hasToString(
                "SELECT a.id, COUNT (p.id) AS inhabitantCount " +
                "FROM Addresses a " +
                "LEFT JOIN People p ON a.id = p.address_id " + 
                "GROUP BY a.id, a.street, a.city");
    }
    
    @Test
    public void joined_view() {
        MiDBStringBuilder qryString = new MiDBStringBuilder();
        
        TemplateLayer<SelectBuilder> tmpl = addresses.getSelectLayer();
        RequestComposer<SelectBuilder> c = new SimpleRequestComposer<>(tmpl.build());
        
        c.requireAll("id", "people.id");
        c.build(qryString.as(syntax, SqlDQML.select()));
        
        qryString.close();
        assertThat(qryString).hasToString(
                "SELECT a.id, COUNT (p.id) AS inhabitantCount " +
                "FROM Addresses a " +
                "LEFT JOIN People p ON a.id = p.address_id " + 
                "GROUP BY a.id, a.street, a.city");
    }

//    SelectTemplate selectTemplate;
//    
//    @Before
//    public void setUp() {
//        
//    }
//    
//    @Test
//    public void test() {
////        SelectComposer cmp = new SelectComposer(selectTemplate.adapt(SelectBuilder::getStatement));
//    }
//    
//    static class SelectComposer extends AbstractQueryComposer<SelectBuilder> {
//
//        public SelectComposer(Template<? super SelectBuilder> template) {
//            super(template);
//        }
//
//        @Override
//        public String toString() {
//            SelectBuilder builder = new SelectBuilder();
//            buildStatement(builder);
//            return builder.toString();
//        }
//    }
//    
//    static class SelectBuilder implements StatementHolder<SelectQueryBuilder> {
//        SelectQueryBuilder builder = new AnsiSqlSyntax().newSelectQuery(null);
//
//        @Override
//        public SelectQueryBuilder getStatement() {
//            return builder;
//        }
//
//        @Override
//        public String toString() {
//            return builder.toString();
//        }
//    }
//    
}
