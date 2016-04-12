package org.cthul.miro.db.syntax;

import java.util.function.Consumer;

/**
 * Interface for a statement builder that allows to create
 * typed sub-clauses.
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
