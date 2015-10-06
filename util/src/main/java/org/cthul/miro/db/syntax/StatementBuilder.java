package org.cthul.miro.db.syntax;

/**
 * Interface for a statement builder that allows to create
 * typed sub-clauses.
 * <p>
 * Sub-clauses will be {@linkplain AutocloseableBuilder closed automatically} if
 * this builder is modified in any other way.
 */
public interface StatementBuilder {
    
    <Clause> Clause begin(ClauseType<Clause> type);
}
