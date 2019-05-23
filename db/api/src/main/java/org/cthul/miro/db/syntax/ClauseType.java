package org.cthul.miro.db.syntax;

import java.util.function.Function;
import org.cthul.miro.db.request.StatementBuilder;
import org.cthul.miro.util.Key;

/**
 * Defines a statement clause.
 * @param <Clause>
 */
public interface ClauseType<Clause> extends Key<Clause> {

    default Clause createDefaultClause(Syntax syntax, StatementBuilder stmt) {
        throw new UnsupportedOperationException(
                (syntax != null ? syntax : "Unknown syntax") + 
                        ": Unsupported clause type " + this + " for " + stmt);
    }
    
    @SuppressWarnings("Convert2Lambda")
    static <T> ClauseType<T> fromStatement(Function<? super StatementBuilder, ? extends T> defaultClause) {
        return new ClauseType<T>() {
            @Override
            public T createDefaultClause(Syntax syntax, StatementBuilder stmt) {
                return defaultClause.apply(stmt);
            }
        };
    }
}
