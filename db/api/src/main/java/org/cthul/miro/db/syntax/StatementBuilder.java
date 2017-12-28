package org.cthul.miro.db.syntax;

import java.util.function.Consumer;
import org.cthul.miro.db.stmt.MiDBString;

/**
 * Interface for a statement builder that allows to create
 * typed sub-clauses. 
 * A {@link StatementBuilder} is a {@link MiDBString} with a {@link Syntax}.
 * <p>
 * Sub-clauses will be {@linkplain AutocloseableBuilder closed automatically} if
 * this builder is modified in any other way.
 */
public interface StatementBuilder {
    
    <Clause> Clause begin(ClauseType<Clause> type);
    
    default <Clause> StatementBuilder clause(ClauseType<Clause> type, Consumer<? super Clause> code) {
        code.accept(begin(type));
        return this;
    }
}
