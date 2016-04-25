package org.cthul.miro.sql.syntax;

import org.cthul.miro.db.syntax.Syntax;
import org.cthul.miro.db.syntax.SyntaxProvider;

/**
 *
 */
public class AnsiSqlProvider implements SyntaxProvider {

    @Override
    public <S extends Syntax> S handle(Class<S> expectedType, String dbString) {
        return null;
    }

    @Override
    public <S extends Syntax> S handleDefault(Class<S> expectedType, String dbString) {
        if (!expectedType.isAssignableFrom(AnsiSqlSyntax.class)) {
            return null;
        }
        return expectedType.cast(new AnsiSqlSyntax());
    }
}
