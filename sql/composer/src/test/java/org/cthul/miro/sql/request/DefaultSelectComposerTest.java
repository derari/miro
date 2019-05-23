package org.cthul.miro.sql.request;

import org.cthul.miro.composer.RequestComposer;
import org.cthul.miro.sql.composer.model.SqlTemplates;
import org.cthul.miro.sql.composer.SelectComposer;
import org.cthul.miro.db.string.MiDBStringBuilder;
import org.cthul.miro.db.string.SyntaxStringBuilder;
import org.cthul.miro.sql.SqlDQML;
import org.cthul.miro.sql.composer.node.DefaultSelectComposer;
import org.cthul.miro.sql.syntax.AnsiSqlSyntax;
import org.cthul.miro.sql.syntax.SqlSyntax;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class DefaultSelectComposerTest {
    
    SqlSyntax syntax = new AnsiSqlSyntax();
    
    SqlTemplates people = new SqlTemplates("People")
            .attributes("p.`id`, p.first_name AS firstName, p.last_name")
            .from("People p");
    
    SelectComposer cmp;
    
    @Before
    public void setUp() {
        cmp = DefaultSelectComposer.create(people);
    }
    
    @Test
    public void test_selectedAttributes() {
        cmp.getSelectedAttributes().addAll("id", "firstName");
        String qry = getQueryString();
        assertThat(qry, is("SELECT p.\"id\", p.first_name AS firstName FROM People p"));
    }
    
    @Test
    public void test_attributeFilter() {
        cmp.getAttributeFilter().put("firstName", "Bob");
        String qry = getQueryString();
        assertThat(qry, is(" FROM People p WHERE p.first_name = ?"));
    }
    
    private String getQueryString() {
        SyntaxStringBuilder qryString = new SyntaxStringBuilder(syntax);
        ((RequestComposer) cmp).build(qryString.begin(SqlDQML.select()));
        qryString.close();
        return qryString.toString();
    }
}
