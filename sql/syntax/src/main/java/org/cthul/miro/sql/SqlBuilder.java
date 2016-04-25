package org.cthul.miro.sql;

import java.util.Collection;
import java.util.function.Consumer;
import org.cthul.miro.sql.SqlClause.In;
import org.cthul.miro.sql.SqlClause.IsNull;
import org.cthul.miro.sql.syntax.MiSqlParser;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.QlCode;

/**
 *
 * @param <This>
 */
public interface SqlBuilder<This extends SqlBuilder<This>> extends QlBuilder<This> {
    
    default This append(Code<? super This> code) {
        This me = (This) this;
        code.appendTo(me);
        return me;
    }
    
    default In<This> in() {
        return (In) begin(SqlClause.in());
    }
    
    default This in(Consumer<? super In<?>> action) {
        return clause(SqlClause.in(), action);
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
        return (IsNull) begin(SqlClause.isNull());
    }
    
    default This isNull(Consumer<? super IsNull<?>> action) {
        return clause(SqlClause.isNull(), action);
    }
    
    default This eq(Object value) {
        return append(" = ?").pushArgument(value);
    }
    
    default This lt(Object value) {
        return append(" < ?").pushArgument(value);
    }
    
    default This gt(Object value) {
        return append(" > ?").pushArgument(value);
    }
    
    default This le(Object value) {
        return append(" <= ?").pushArgument(value);
    }
    
    default This ge(Object value) {
        return append(" >= ?").pushArgument(value);
    }
    
    default This sql(String sql) {
        QlCode c = MiSqlParser.parseCode(sql);
        return append(c);
    }
    
    default This sql(String sql, Object... args) {
        return sql(sql).pushArguments(args);
    }
    
    default This sql(String sql, Iterable<?> args) {
        return sql(sql).pushArguments(args);
    }
    
    interface Code<B> extends Consumer<B> {

        @Override
        default void accept(B clause) {
            appendTo(clause);
        }
        
        void appendTo(B c);
    }
}
