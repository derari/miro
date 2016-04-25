package org.cthul.miro.ext.mysql;

import org.cthul.miro.sql.syntax.AnsiSqlSyntax;
import org.cthul.miro.db.stmt.MiDBString;
import org.cthul.miro.db.syntax.QlBuilder;

/**
 *
 */
public class MySqlSyntax extends AnsiSqlSyntax {

    @Override
    public QlBuilder<?> newQlBuilder(MiDBString dbString) {
        return new MySqlBuilder(this, dbString);
    }
}
