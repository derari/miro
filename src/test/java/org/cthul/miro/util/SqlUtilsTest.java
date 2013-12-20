package org.cthul.miro.util;

import java.util.List;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class SqlUtilsTest {
    
    @Test
    public void test_splitSelectClause_simple() {
        String select = "a , a.b,basc,b as c";
        List<String> parts = SqlUtils.splitSelectClause(select);
        assertThat(parts, contains("a", "a.b", "basc", "b as c"));
    }
    
    @Test
    public void test_splitSelectClause_quoted() {
        String select = "'a' , 'x,\"',``";
        List<String> parts = SqlUtils.splitSelectClause(select);
        assertThat(parts, contains("'a'", "'x,\"'", "``"));
    }
    
    @Test
    public void test_splitSelectClause_brackets() {
        String select = "max(a) as max_a , foo(')', 1)";
        List<String> parts = SqlUtils.splitSelectClause(select);
        assertThat(parts, contains("max(a) as max_a", "foo(')', 1)"));
    }
    
    @Test
    public void test_parseSelectPart_simple() {
        String part = "key";
        String[] keyDef = SqlUtils.parseSelectPart(part);
        assertThat(keyDef, arrayContaining("key", "key", null, "key", "key", "key"));
    }
    
    @Test
    public void test_parseSelectPart_quoted() {
        String part = " 'key' ";
        String[] keyDef = SqlUtils.parseSelectPart(part);
        assertThat(keyDef, arrayContaining("key", "'key'", null, "key", "'key'", "'key'"));
    }
    
    @Test
    public void test_parseSelectPart_qualified() {
        String part = "table . key";
        String[] keyDef = SqlUtils.parseSelectPart(part);
        assertThat(keyDef, arrayContaining("key", part, "table", "key", "key", "key"));
    }
    
    @Test
    public void test_parseSelectPart_simple_not_as() {
        String part = "valueASkey";
        String[] keyDef = SqlUtils.parseSelectPart(part);
        assertThat(keyDef, arrayContaining("valueASkey", part, null, "valueASkey", "valueASkey", "valueASkey"));
    }
    
    @Test
    public void test_parseSelectPart_simple_as() {
        String part = "value as key";
        String[] keyDef = SqlUtils.parseSelectPart(part);
        assertThat(keyDef, arrayContaining("key", "value", null, "value", "key", "value"));
    }
    
    @Test
    public void test_parseSelectPart_qualified_as() {
        String part = "'table'.`value` as 'key'";
        String[] keyDef = SqlUtils.parseSelectPart(part);
        assertThat(keyDef, arrayContaining("key", "'table'.`value`", "table", "value", "'key'", "`value`"));
    }
    
    @Test
    public void test_parseSelectPart_complex() {
        String part = "foo(value, '(AS') AS key";
        String[] keyDef = SqlUtils.parseSelectPart(part);
        assertThat(keyDef, arrayContaining("key", "foo(value, '(AS')", null, null, "key", null));
    }
    
    @Test
    public void test_parseAttributePart_simple() {
        String part = "key";
        String[] keyDef = SqlUtils.parseAttributePart(part);
        assertThat(keyDef, arrayContaining("key", "key", null, "key", "key", "key"));
    }
    
    @Test
    public void test_parseAttributePart_quoted() {
        String part = " 'key' ";
        String[] keyDef = SqlUtils.parseAttributePart(part);
        assertThat(keyDef, arrayContaining("key", "'key'", null, "key", "'key'", "'key'"));
    }
    
    @Test
    public void test_parseAttributePart_qualified() {
        String part = "table . key";
        String[] keyDef = SqlUtils.parseAttributePart(part);
        assertThat(keyDef, arrayContaining("key", part, "table", "key", "key", "key"));
    }
    
    @Test
    public void test_parseAttributePart_simple_not_as() {
        String part = "valueASkey";
        String[] keyDef = SqlUtils.parseAttributePart(part);
        assertThat(keyDef, arrayContaining("valueASkey", part, null, "valueASkey", "valueASkey", "valueASkey"));
    }
    
    @Test
    public void test_parseAttributePart_simple_as() {
        String part = "value as key";
        String[] keyDef = SqlUtils.parseAttributePart(part);
        assertThat(keyDef, arrayContaining("key", "value", null, "value", "key", "value"));
    }
    
    @Test
    public void test_parseAttributePart_qualified_as() {
        String part = "'table'.`value` as 'key'";
        String[] keyDef = SqlUtils.parseAttributePart(part);
        assertThat(keyDef, arrayContaining("key", "'table'.`value`", "table", "value", "'key'", "`value`"));
    }
    
    @Test
    public void test_parseFromPart_simple() {
        String part = "key";
        String[] keyDef = SqlUtils.parseFromPart(part);
        assertThat(keyDef, arrayContaining("key", part, "key"));
    }
    
    @Test
    public void test_parseFromPart_key() {
        String part = "table key";
        String[] keyDef = SqlUtils.parseFromPart(part);
        assertThat(keyDef, arrayContaining("key", part, "table"));
    }
    
    @Test
    public void test_parseFromPart_complex() {
        String part = "(SELECT * FROM table WHERE x = ?) key";
        String[] keyDef = SqlUtils.parseFromPart(part);
        assertThat(keyDef, arrayContaining("key", part, "(SELECT * FROM table WHERE x = ?)"));
    }
    
    @Test
    public void test_parseOrderPart_simple() {
        String part = "field";
        String[] def = SqlUtils.parseOrderPart(part);
        assertThat(def, arrayContaining("field", part, null));
    }
    
    @Test
    public void test_parseOrderPart_complex() {
        String part = "`a`.'field' DESC";
        String[] def = SqlUtils.parseOrderPart(part);
        assertThat(def, arrayContaining("field", part, "a.field"));
    }    
}