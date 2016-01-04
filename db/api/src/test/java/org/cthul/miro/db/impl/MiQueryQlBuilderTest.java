package org.cthul.miro.db.impl;

import static org.cthul.matchers.fluent8.FluentAssert.assertThat;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.syntax.TestConnection;
import org.cthul.miro.db.syntax.TestSyntax;
import org.junit.Test;

/**
 *
 */
public class MiQueryQlBuilderTest {
    
    @Test
    public void test_query_is_closed() throws MiException {
        TestConnection cnn = new TestConnection();
        MiQueryQlBuilder queryBuilder = MiQueryQlBuilder.create(cnn);
        queryBuilder.ql("foo ");
        queryBuilder.begin(TestSyntax.IN_PARENT).ql("bar");
        queryBuilder.execute();
        assertThat(cnn.getLastQuery()).is("foo (bar)");
    }
}
