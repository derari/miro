package org.cthul.miro.db.syntax;

import org.cthul.miro.db.request.StatementBuilder;
import java.util.function.Consumer;
import org.cthul.miro.db.string.MiDBString;

/**
 * Interface for building queries from identifiers, string literals,
 * and native query language.
 * Is used to abstract language flavors.
 * @param <This>
 */
public interface QlBuilder<This extends QlBuilder<This>> extends MiDBString, StatementBuilder {
    
    @Override
    This append(CharSequence query);
    
    default This ql(String query) {
        return append(query);
    }
    
    default This ql(String query, Object... args) {
        return append(query).pushArguments(args);
    }
    
    default This append(QlCode code) {
        code.appendTo(this);
        return (This) this;
    }
    
    default This ql(QlCode code) {
        return append(code);
    }
    
    default This appendAll(Object... code) {
        This self = (This) this;
        for (Object c: code) {
            if (c instanceof String) {
                self = self.ql((String) c);
            } else if (c instanceof QlCode) {
                self = self.append((QlCode) c);
            } else {
                throw new IllegalArgumentException("Expected String or QlCode, got " + c);
            }
        }
        return self;
    }
    
    This identifier(String id);
    
    default This id(String id) {
        return identifier(id);
    }
    
    default This id(String... id) {
        This me = (This) this;
        for (int i = 0; i < id.length; i++) {
            if (i > 0) me = me.ql(".");
            me = me.id(id[i]);
        }
        return me;
    }
    
    default This namedTable(String table, String name) {
        return id(table).ql(" ").id(name);
    }
    
    default This attribute(String id, String attribute) {
        return id(id, attribute);
    }
    
    This stringLiteral(String string);
    
    This constant(Object key);
    
    @Override
    default <Clause> This clause(ClauseType<Clause> type, Consumer<? super Clause> code) {
        code.accept(begin(type));
        return (This) this;
    }
    
    @Override
    This pushArgument(Object arg);

    @Override
    default This pushArguments(Iterable<?> args) {
        return (This) MiDBString.super.pushArguments(args);
    }

    @Override
    default This pushArguments(Object... args) {
        return (This) MiDBString.super.pushArguments(args);
    }
    
    interface Open extends OpenClause {

        @Override
        <T> QlBuilder<?> open(T parent);
    }
    
    final ClauseType<QlBuilder<?>> TYPE = new ClauseType<QlBuilder<?>>() {};
    
    static QlBuilder<?> create(StatementBuilder stmt) {
        return stmt.begin(TYPE);
    }
}
