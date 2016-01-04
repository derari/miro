package org.cthul.miro.db.sql;

import java.util.Collection;
import java.util.function.Consumer;
import org.cthul.miro.db.sql.SqlClause.In;
import org.cthul.miro.db.sql.SqlClause.IsNull;
import org.cthul.miro.db.sql.syntax.MiSqlParser;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.QlCode;

/**
 *
 */
public interface SqlBuilder<This extends SqlBuilder<This>> extends QlBuilder<This> {
    
    default In<This> in() {
        return (In) begin(SqlClause.IN);
    }
    
    default This in(Consumer<? super In<?>> action) {
        return clause(SqlClause.IN, action);
    }
    
    default This in(int length) {
        return in(in -> in.setLength(length));
    }
    
    default This in(Collection<?> arguments) {
        return in(in -> in.list(arguments));
    }
    
    default This in(Object... arguments) {
        return in(in -> in.list(arguments));
    }
    
    default IsNull<This> isNull() {
        return (IsNull) begin(SqlClause.IS_NULL);
    }
    
    default This isNull(Consumer<? super IsNull<?>> action) {
        return clause(SqlClause.IS_NULL, action);
    }
    
    default This sql(String sql) {
        QlCode c = MiSqlParser.parseExpression(sql);
        return append(c);
    }
    
    default This sql(String sql, Object... args) {
        return sql(sql).pushArguments(args);
    }
    
    default This sql(String sql, Iterable<?> args) {
        return sql(sql).pushArguments(args);
    }
}
