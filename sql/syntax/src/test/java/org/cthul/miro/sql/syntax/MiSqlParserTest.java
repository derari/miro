package org.cthul.miro.sql.syntax;

import java.util.Arrays;
import java.util.List;
import static org.cthul.matchers.fluent8.FluentAssert.assertThat;
import org.cthul.miro.db.string.MiDBStringBuilder;
import org.cthul.miro.db.string.SyntaxStringBuilder;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.QlCode;
import org.cthul.miro.sql.syntax.MiSqlParser.Attribute;
import org.cthul.miro.sql.syntax.MiSqlParser.SelectStmt;
import org.junit.Test;

/**
 *
 */
public class MiSqlParserTest {
    
    public MiSqlParserTest() {
    }

    @Test
    public void attribute_basic() {
        Attribute a = MiSqlParser.parseAttribute("foo.bar as baz");
        assertThat(a).isNot().nullValue();
        assertThat(a.getKey()).is("baz");
        assertThat(a.getAlias()).hasToString("baz");
        assertThat(a.getColumnRefs()).hasSize(1);
        assertThat(a.getColumnRefs().get(0).getExpression()).hasToString("[foo.bar]");
    }

    @Test
    public void attribute_basic_wo_AS() {
        Attribute a = MiSqlParser.parseAttribute("foo.bar `baz`");
        assertThat(a).isNot().nullValue();
        assertThat(a.getKey()).is("baz");
        assertThat(a.getAlias()).hasToString("[baz]");
        assertThat(a.getColumnRefs()).hasSize(1);
        assertThat(a.getColumnRefs().get(0).getExpression()).hasToString("[foo.bar]");
    }
    
    @Test
    public void attribute_no_alias() {
        Attribute a = MiSqlParser.parseAttribute("foo.`bar`");
        assertThat(a).isNot().nullValue();
        assertThat(a.getKey()).is("bar");
        assertThat(a.getAlias()).is().nullValue();
        assertThat(a.getColumnRefs()).hasSize(1);
        assertThat(a.getColumnRefs().get(0).getExpression()).hasToString("[foo., [bar]]");
    }
    
    @Test
    public void attributes() {
        List<Attribute> list = MiSqlParser.parseAttributes("foo.bar, 1 AS baz");
        assertThat(list).hasSize(2);
    }
    
    @Test
    public void function() {
        String sql = "MAX(foo)";
        String parsed = MiSqlParser.parseCode(sql).toString();
        assertThat(parsed).is("[MAX(foo)]");
    }
    
    @Test
    public void code_leading_space() {
        String sql = " foo";
        String parsed = MiSqlParser.parseCode(sql).toString();
        assertThat(parsed).is("[ foo]");
    }
    
    @Test
    public void code_trailing_space() {
        String sql = "foo ";
        String parsed = MiSqlParser.parseCode(sql).toString();
        assertThat(parsed).is("[foo ]");
    }
    
    @Test
    public void partialOrCode_code_with_args() {
        String sql = "(?";
        Object[] args = {1};
        TestBuilder ql = new TestBuilder();
        MiSqlParser.parsePartialSelectOrCode(sql, args, "", null, ql);
        assertThat(ql).hasToString("[(?]");
        assertThat(ql.getArgs()).hasSize(1);
    }
    
    @Test
    public void partialSelect() {
        String sql = "SELECT a, `b`.`c` AS d, z.x "
                + "FROM `Foo` f "
                + "JOIN (SELECT x, y FROM `Baz`) z ON f.id = z.id "
                + "WHERE f.a > 0 "
                + "GROUP BY f.id, z.id "
                + "HAVING SUM(b) > 0 AND NOT(x IS NULL)"
                + "ORDER BY c, d ASC ";
        SelectStmt stmt = MiSqlParser.parsePartialSelect(sql);
        assertThat(stmt).isNot().nullValue();
    }
    
    @Test
    public void macro_is_null() {
        QlCode c = MiSqlParser.parseCode("@IS_NULL{`foo`}");
        SyntaxStringBuilder string = new SyntaxStringBuilder(new AnsiSqlSyntax());
        string.begin(QlBuilder.TYPE).append(c);
        string.close();
        assertThat(string.toString()).is("(\"foo\" IS NULL)");
    }
    
    @Test
    public void macro_in() {
        QlCode c = MiSqlParser.parseCode("`foo` @IN{?}", Arrays.asList(1, 2));
        MiDBStringBuilder dbString = new MiDBStringBuilder();
        SyntaxStringBuilder string = new SyntaxStringBuilder(new AnsiSqlSyntax(), dbString);
        string.begin(QlBuilder.TYPE).append(c);
        string.close();
        assertThat(dbString.toString()).is("\"foo\" IN (?,?)");
        assertThat(dbString.getArguments()).contains(1, 2);
    }
    
    static class TestBuilder extends QlCode.Builder {

        public List<Object> getArgs() {
            return super.args();
        }
    }
}
