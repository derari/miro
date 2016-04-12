package org.cthul.miro.db.impl;

import org.cthul.miro.db.MiException;
import org.cthul.miro.db.syntax.TestConnection;
import org.cthul.miro.db.syntax.TestSyntax;
import org.junit.Test;
import static org.cthul.matchers.fluent8.FluentAssert.assertThat;

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
    

}
