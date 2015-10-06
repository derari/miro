package org.cthul.miro.db.sql.syntax;

import org.cthul.miro.db.syntax.AbstractQlBuilder;
import org.cthul.miro.db.syntax.CoreStmtBuilder;
import org.cthul.miro.db.syntax.Syntax;

/**
 *
 */
public class MySqlBuilder extends AbstractQlBuilder<MySqlBuilder> {

    public MySqlBuilder(Syntax syntax, CoreStmtBuilder coreBuilder) {
        super(syntax, coreBuilder);
    }

    @Override
    public MySqlBuilder identifier(String id) {
        return append("`").append(id).append("`");
    }

    @Override
    public MySqlBuilder stringLiteral(String string) {
        return append("'").append(
                string.replace("\\", "\\\\")
                      .replace("'", "\\'")
        ).append("'");
    }
}
