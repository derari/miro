package org.cthul.miro.db.request;

import java.util.function.Consumer;
import java.util.function.Function;
import org.cthul.miro.db.syntax.ClauseType;

/**
 * Interface for a statement builder that allows to create
 * typed sub-clauses. 
 * <p>
 * Sub-clauses will be {@linkplain AutocloseableBuilder closed automatically} if
 * this builder is modified in any other way.
 */
public interface StatementBuilder {
    
    <Clause> Clause begin(ClauseType<Clause> type);
    
    <Clause> Clause as(Function<StatementBuilder, Clause> factory);
    
    default <Clause> StatementBuilder clause(ClauseType<Clause> type, Consumer<? super Clause> code) {
        code.accept(begin(type));
        return this;
    }
}
