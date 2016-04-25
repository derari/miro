package org.cthul.miro.ext.hana;

import org.cthul.miro.db.syntax.Syntax;
import org.cthul.miro.db.syntax.SyntaxProvider;

/**
 *
 */
public class HanaSqlSyntaxProvider implements SyntaxProvider {

    @Override
    public <S extends Syntax> S handle(Class<S> expectedType, String dbString) {
        if (dbString.startsWith("jdbc:sap:")) {
            return handleDefault(expectedType, dbString);
        }
        return null;
    }

    @Override
    public <S extends Syntax> S handleDefault(Class<S> expectedType, String dbString) {
        if (!expectedType.isAssignableFrom(HanaSqlSyntax.class)) {
            return null;
        }
        return expectedType.cast(new HanaSqlSyntax());
    }
}
