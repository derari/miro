package org.cthul.miro.db.sql.syntax;

import java.util.List;
import static org.cthul.matchers.fluent8.FluentAssert.assertThat;
import org.cthul.miro.db.sql.syntax.MiSqlParser.Attribute;
import org.cthul.miro.db.sql.syntax.MiSqlParser.SelectStmt;
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
}
