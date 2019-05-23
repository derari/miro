package org.cthul.miro.db.impl;

import java.util.function.Consumer;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.syntax.TestConnection;
import org.cthul.miro.db.syntax.TestSyntax;
import org.junit.Test;
import static org.cthul.matchers.fluent8.FluentAssert.assertThat;
import org.cthul.miro.db.request.AutocloseableBuilder;
import org.cthul.miro.db.request.StatementBuilder;
import org.cthul.miro.db.syntax.ClauseType;
import org.cthul.miro.db.syntax.Syntax;

/**
 *
 */
public class MiQueryQlBuilderTest {
    
    @Test
    public void test_query_is_closed() throws MiException {
        TestConnection cnn = new TestConnection();
        SimpleMiQueryString queryBuilder = new SimpleMiQueryString(cnn.newQuery(), cnn.syntax);
        queryBuilder.ql("foo ");
        queryBuilder.begin(TestSyntax.IN_PARENTHESES).ql("bar");
        queryBuilder.execute();
        assertThat(cnn.getLastQuery()).is("foo (bar)");
    }
    
    @Test
    public void test_nested_closing() throws MiException {
        TestConnection cnn = new TestConnection();
        SimpleMiQueryString queryBuilder = new SimpleMiQueryString(cnn.newQuery(), cnn.syntax);
        queryBuilder.ql("foo ");
        queryBuilder.begin(TestSyntax.IN_PARENTHESES).begin(new DeferredClause()).accept("bar");
        queryBuilder.execute();
        assertThat(cnn.getLastQuery()).is("foo ((bar))");        
    }
    
    private static final class DeferredClause implements ClauseType<Consumer<String>> {

        @Override
        public Consumer<String> createDefaultClause(Syntax syntax, StatementBuilder stmt) {
            class Clause implements Consumer<String>, AutocloseableBuilder {
                String value;
                @Override
                public void accept(String t) {
                    value = t;
                }
                @Override
                public void close() {
                    stmt.begin(TestSyntax.IN_PARENTHESES).append(value);
                }
            }
            return new Clause();
        }
    }
}
