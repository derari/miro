package org.cthul.miro.db.sql;

import org.cthul.miro.db.MiException;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 *
 */
public class StandardCreateStatementTest {

    @Test
    public void test_create() throws MiException {
        CreateStatement create = CreateStatement.create(new TestConnection());
        create.table("Foo");
        create.execute();
        assertThat(TestConnection.lastQuery, is("CREATE TABLE \"Foo\" ()"));
    }
    
}
