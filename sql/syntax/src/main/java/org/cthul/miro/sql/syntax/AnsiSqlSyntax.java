package org.cthul.miro.sql.syntax;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.cthul.miro.db.impl.AbstractNestedBuilder;
import org.cthul.miro.sql.SqlClause;
import org.cthul.miro.db.request.MiDBString;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.QlCode;
import org.cthul.miro.db.syntax.Syntax;

/**
 *
 */
public class AnsiSqlSyntax implements SqlSyntax {
    
    private final Map<String, QlCode> constants = new HashMap<>();
    private final Map<String, String> tableSchemas = new HashMap<>();
    
    public AnsiSqlSyntax putConst(Object key, QlCode value) {
        constants.put(String.valueOf(key), value);
        return this;
    }
    
    public AnsiSqlSyntax schema(String schema, String... tables) {
        return schema(schema, Arrays.asList(tables));
    }
    
    public AnsiSqlSyntax schema(String schema, Iterable<String> tables) {
        tables.forEach(t -> tableSchemas.put(schema, t));
        return this;
    }

    @Override
    public void appendConstant(Object key, QlBuilder<?> query) {
        QlCode c = constants.get(String.valueOf(key));
        if (c == null) throw new IllegalArgumentException(String.valueOf(key));
        c.appendTo(query);
    }

    @Override
    public QlBuilder<?> newQlBuilder(MiDBString dbString) {
        return new AnsiSqlBuilder(this, dbString);
    }

    @Override
    public <O> SqlClause.IsNull<O> newIsNull(MiDBString dbString, O owner) {
        return new IsNull<>(owner, dbString, this);
    }

    @Override
    public <O> SqlClause.In<O> newIn(MiDBString dbString, O owner) {
        return new In(owner, dbString, this);
    }
    
    public static class In<Owner> extends AbstractNestedBuilder<Owner, In<Owner>> implements SqlClause.In<Owner> {

        private int length = 0;
        
        public In(Owner owner, MiDBString dbString, SqlSyntax syntax) {
            super(owner, syntax.asQlBuilder(dbString), syntax);
        }

        @Override
        public SqlClause.In setLength(int length) {
            this.length = length;
            return this;
        }

        @Override
        public SqlClause.In push(Object argument) {
            this.length ++;
            pushArgument(argument);
            return this;
        }

        @Override
        public SqlClause.In list(Collection<?> arguments) {
            this.length += arguments.size();
            pushArguments(arguments);
            return this;
        }

        @Override
        public void close() {
            if (length < 1) {
                throw new IllegalStateException("Length expected");
            }
            ql(" IN (?");
            for (int i = 1; i < length; i++) {
                ql(",?");
            }
            ql(")");
            super.close();
        }
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
        }

        @Override
        public void close() {
            ql(" IS NULL)");
            super.close();
        }
    }
}
