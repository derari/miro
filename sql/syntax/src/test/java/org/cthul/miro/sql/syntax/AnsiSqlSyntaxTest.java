package org.cthul.miro.sql.syntax;

import static org.cthul.matchers.fluent8.FluentAssert.assertThat;
import org.cthul.miro.db.impl.MiDBStringBuilder;
import org.cthul.miro.sql.SqlClause;
import org.cthul.miro.db.stmt.MiDBString;
import org.cthul.miro.db.syntax.QlBuilder;
import org.junit.Test;

public class AnsiSqlSyntaxTest {

    private final MiDBString dbString = new MiDBStringBuilder();
    private final AnsiSqlSyntax syntax = new AnsiSqlSyntax();
    private final QlBuilder<?> ql = syntax.newQlBuilder(dbString);
    
    @Test
    public void test_in() {
        ql.id("foo");
        ql.begin(SqlClause.in()).setLength(3);
        ql.append("");
        assertThat(dbString.toString())
                .is("\"foo\" IN (?,?,?)");
    }
}
