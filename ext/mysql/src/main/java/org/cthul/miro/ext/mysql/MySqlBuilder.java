package org.cthul.miro.ext.mysql;

import org.cthul.miro.db.request.StatementBuilder;
import org.cthul.miro.db.string.AbstractQlBuilder;
import org.cthul.miro.db.syntax.Syntax;

/**
 *
 */
public class MySqlBuilder extends AbstractQlBuilder<MySqlBuilder> {

    public MySqlBuilder(Syntax syntax, StatementBuilder stmt) {
        super(syntax, stmt);
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
