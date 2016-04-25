package org.cthul.miro.ext.mysql;

import org.cthul.miro.db.syntax.Syntax;
import org.cthul.miro.db.syntax.SyntaxProvider;

/**
 *
 */
public class MySqlSyntaxProvider implements SyntaxProvider {

    @Override
    public <S extends Syntax> S handle(Class<S> expectedType, String dbString) {
        if (dbString.startsWith("jdbc:mysql:")) {
            return handleDefault(expectedType, dbString);
        }
        return null;
    }

    @Override
    public <S extends Syntax> S handleDefault(Class<S> expectedType, String dbString) {
        if (!expectedType.isAssignableFrom(MySqlSyntax.class)) {
            return null;
        }
        return expectedType.cast(new MySqlSyntax());
    }
    
}
