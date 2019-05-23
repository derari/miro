package org.cthul.miro.ext.mysql;

import org.cthul.miro.sql.syntax.AnsiSqlSyntax;
import org.cthul.miro.db.request.StatementBuilder;
import org.cthul.miro.db.syntax.QlBuilder;

/**
 *
 */
public class MySqlSyntax extends AnsiSqlSyntax {

    @Override
    public QlBuilder<?> newQlBuilder(StatementBuilder stmt) {
        return new MySqlBuilder(this, stmt);
    }
}
