package org.cthul.miro.db.sql.syntax;

import org.cthul.miro.db.syntax.CoreStmtBuilder;
import org.cthul.miro.db.syntax.QlBuilder;

/**
 *
 */
public class AnsiSqlSyntax implements SqlSyntax {

    @Override
    public QlBuilder<?> newQlBuilder(CoreStmtBuilder coreBuilder) {
        return new AnsiSqlBuilder(this, coreBuilder);
    }
}
