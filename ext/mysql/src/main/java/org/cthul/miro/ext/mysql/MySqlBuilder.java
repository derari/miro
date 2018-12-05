package org.cthul.miro.ext.mysql;

import org.cthul.miro.db.impl.AbstractQlBuilder;
import org.cthul.miro.db.request.MiDBString;
import org.cthul.miro.db.syntax.Syntax;

/**
 *
 */
public class MySqlBuilder extends AbstractQlBuilder<MySqlBuilder> {

    public MySqlBuilder(Syntax syntax, MiDBString dbString) {
        super(syntax, dbString);
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
