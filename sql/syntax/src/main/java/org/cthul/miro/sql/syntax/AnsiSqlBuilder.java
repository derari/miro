package org.cthul.miro.sql.syntax;

import org.cthul.miro.db.impl.AbstractQlBuilder;
import org.cthul.miro.db.stmt.MiDBString;
import org.cthul.miro.db.syntax.Syntax;

/**
 *
 */
public class AnsiSqlBuilder extends AbstractQlBuilder<AnsiSqlBuilder> {

    public AnsiSqlBuilder(Syntax syntax, MiDBString dbString) {
        super(syntax, dbString);
    }

    @Override
    public AnsiSqlBuilder identifier(String id) {
        return append("\"").append(
                id.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
            ).append("\"");
    }

    @Override
    public AnsiSqlBuilder stringLiteral(String string) {
        return append("'").append(
                string.replace("\\", "\\\\")
                      .replace("'", "\\'")
            ).append("'");
    }
}
