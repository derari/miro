package org.cthul.miro.db.sql.syntax;

import org.cthul.miro.db.syntax.AbstractQlBuilder;
import org.cthul.miro.db.syntax.CoreStmtBuilder;
import org.cthul.miro.db.syntax.Syntax;

/**
 *
 */
public class AnsiSqlBuilder extends AbstractQlBuilder<AnsiSqlBuilder> {

    public AnsiSqlBuilder(Syntax syntax, CoreStmtBuilder coreBuilder) {
        super(syntax, coreBuilder);
    }

    @Override
    public AnsiSqlBuilder identifier(String id) {
        return append("\"").append(id).append("\"");
    }

    @Override
    public AnsiSqlBuilder stringLiteral(String string) {
        return append("'").append(
                string.replace("\\", "\\\\")
                      .replace("'", "\\'")
        ).append("'");
    }
}
