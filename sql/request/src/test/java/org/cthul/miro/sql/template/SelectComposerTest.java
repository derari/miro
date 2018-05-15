package org.cthul.miro.sql.template;

import java.util.function.Function;
import org.cthul.miro.db.impl.MiDBStringBuilder;
import org.cthul.miro.request.ComposerState;
import org.cthul.miro.request.StatementPart;
import org.cthul.miro.sql.SelectBuilder;
import org.cthul.miro.sql.SqlDQML;
import org.cthul.miro.sql.syntax.AnsiSqlSyntax;
import org.cthul.miro.sql.syntax.SqlSyntax;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class SelectComposerTest {
    
    SqlSyntax syntax = new AnsiSqlSyntax();
    
    SqlTemplates people = new SqlTemplates("People")
            .attributes("p.`id`, p.first_name AS firstName, p.last_name")
            .from("People p");
    
    SelectComposer cmp;
    
    @Before
    public void setUp() {
        cmp = new SelectNodeFactory(people).newComposer();
    }
    
    @Test
    public void test() {
        cmp.getSelectedAttributes().addAll("id", "firstName");
        String qry = getQueryString();
        assertThat(qry, is("SELECT p.\"id\", p.first_name AS firstName FROM People p"));
    }
    
    private String getQueryString() {
        MiDBStringBuilder qryString = new MiDBStringBuilder();
        ComposerState.asRequestComposer(cmp).build(qryString.as(syntax, SqlDQML.select()));
        qryString.close();
        return qryString.toString();
    }
}
