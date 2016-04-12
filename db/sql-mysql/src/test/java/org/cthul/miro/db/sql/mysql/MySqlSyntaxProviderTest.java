package org.cthul.miro.db.sql.mysql;

import static org.cthul.matchers.Fluents.assertThat;
import org.cthul.miro.db.syntax.Syntax;
import org.cthul.miro.db.syntax.SyntaxProvider;
import org.junit.Test;

/**
 *
 */
public class MySqlSyntaxProviderTest {

    @Test
    public void test_handle() {
        String dbString = "jdbc:mysql:";
        Syntax s = SyntaxProvider.find(dbString);
        assertThat(s).isA(MySqlSyntax.class);
    }
}
