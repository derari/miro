package org.cthul.miro.ext.hana;

import org.cthul.miro.db.impl.AbstractNestedBuilder;
import org.cthul.miro.sql.syntax.AnsiSqlSyntax;
import org.cthul.miro.db.request.MiDBString;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.Syntax;
import org.cthul.miro.sql.SqlClause;
import org.cthul.miro.sql.syntax.SqlSyntax;

/**
 *
 */
public class HanaSqlSyntax extends AnsiSqlSyntax {

    @Override
    public QlBuilder<?> newQlBuilder(MiDBString dbString) {
        return new HanaSqlBuilder(this, dbString);
    }

    @Override
    public <O> SqlClause.IsNull<O> newIsNull(MiDBString dbString, O owner) {
        return new IsNull<>(owner, dbString, this);
    }
    
    public static class IsNull<Owner> extends AbstractNestedBuilder<Owner, SqlClause.IsNull<Owner>> implements SqlClause.IsNull<Owner> {

        public IsNull(Owner owner, MiDBString dbString, SqlSyntax syntax) {
            super(owner, syntax.asQlBuilder(dbString), syntax);
        }

        public IsNull(Owner owner, QlBuilder<?> builder, Syntax syntax) {
            super(owner, builder, syntax);
        }

        @Override
        protected void open() {
            super.open();
            ql("(");
        } // "(LEAST(0, IFNULL($1, -1))+1)");

        @Override
        public void close() {
            ql(" IS NOT NULL)");
            super.close();
        }
    }
}
