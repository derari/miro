package org.cthul.miro.db.impl;

import org.cthul.miro.db.stmt.MiDBString;
import org.cthul.miro.db.syntax.ClauseType;
import org.cthul.miro.db.syntax.StatementBuilder;
import org.cthul.miro.db.syntax.Syntax;

/**
 * A {@link StatementBuilder} implementation.
 */
public abstract class AbstractStatementBuilder extends AbstractComposableBuilder implements StatementBuilder {
    
    private final Syntax syntax;

    public AbstractStatementBuilder(Syntax syntax) {
        this.syntax = syntax;
    }

    protected Syntax getSyntax() {
        return syntax;
    }
    
    protected <Clause> Clause newClause(MiDBString dbString, ClauseType<Clause> type, Object owner) {
       return getSyntax().newClause(dbString, owner, type);
    }
    
    @Override
    public <Clause> Clause begin(ClauseType<Clause> type) {
        return newNestedClause(str -> newClause(str, type, this));
    }
}
