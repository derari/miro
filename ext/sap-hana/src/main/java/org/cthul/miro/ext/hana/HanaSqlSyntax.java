package org.cthul.miro.ext.hana;

import org.cthul.miro.db.request.StatementBuilder;
import org.cthul.miro.sql.syntax.AnsiSqlSyntax;
import org.cthul.miro.db.syntax.QlBuilder;

/**
 *
 */
public class HanaSqlSyntax extends AnsiSqlSyntax {

    @Override
    public QlBuilder<?> newQlBuilder(StatementBuilder stmt) {
        return new HanaSqlBuilder(this, stmt);
    }

//    @Override
//    @SuppressWarnings("Convert2Lambda")
//    public SqlClause.OpenIsNull newIsNull(StatementBuilder stmt) {
//        return new SqlClause.OpenIsNull() {
//            @Override
//            public <T> SqlClause.IsNull<T> open(T parent) {
//                return stmt.as(nested -> new IsNull<>(parent, nested, HanaSqlSyntax.this));
//            }
//        };
//    }
//
//    public static class IsNull<Owner> extends AbstractNestedBuilder<Owner, SqlClause.IsNull<Owner>> implements SqlClause.IsNull<Owner> {
//
//        public IsNull(Owner owner, StatementBuilder stmt, SqlSyntax syntax) {
//            super(owner, stmt, syntax);
//        }
//
//        public IsNull(Owner owner, QlBuilder<?> builder, Syntax syntax) {
//            super(owner, builder, syntax);
//        }
//
//        @Override
//        protected void open() {
//            super.open();
//            ql("(");
//        } // "(LEAST(0, IFNULL($1, -1))+1)");
//
//        @Override
//        public void close() {
//            ql(" IS NOT NULL)");
//            super.close();
//        }
//    }
}
