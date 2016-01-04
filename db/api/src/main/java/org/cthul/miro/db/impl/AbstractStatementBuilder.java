package org.cthul.miro.db.impl;

import org.cthul.miro.db.stmt.MiDBString;
import org.cthul.miro.db.syntax.ClauseType;
import org.cthul.miro.db.syntax.StatementBuilder;
import org.cthul.miro.db.syntax.Syntax;

/**
 * A statement builder implementation with sub-clause support.
 */
public abstract class AbstractStatementBuilder implements StatementBuilder, AutocloseableBuilder {
    
    private final Syntax syntax;
    private AutocloseableBuilder autocloseableBuilder = NO_CLOSABLE;

    public AbstractStatementBuilder(Syntax syntax) {
        this.syntax = syntax;
    }
    
    protected abstract MiDBString getBuilder();

    protected Syntax getSyntax() {
        return syntax;
    }
    
    protected void checkActive(AutocloseableBuilder active) {
        if (active == null) {
            autocloseableBuilder.close();
            autocloseableBuilder = NO_CLOSABLE;
        } else if (active != autocloseableBuilder) {
            throw new IllegalStateException(active + " is closed");
        }
    }

    @Override
    public void close() {
        closeNestedClause();
    }
    
    protected void closeNestedClause() {
        checkActive(null);
    }
    
    protected <Clause> Clause createClause(MiDBString coreBuilder, ClauseType<Clause> type) {
        return getSyntax().newClause(coreBuilder, this, type);
    }
    
    @Override
    public <Clause> Clause begin(ClauseType<Clause> type) {
        closeNestedClause();
        PublicCoreStmtBuilder pubBuilder = new PublicCoreStmtBuilder();
        Clause clause = createClause(pubBuilder, type);
        if (clause instanceof AutocloseableBuilder) {
            autocloseableBuilder = (AutocloseableBuilder) clause;
            pubBuilder.owner = autocloseableBuilder;
        }
        return clause;
    }

    @Override
    public String toString() {
        return getBuilder().toString();
    }
    
    private class PublicCoreStmtBuilder extends MiDBStringDelegator {
        
        AutocloseableBuilder owner = null;

        @Override
        protected MiDBString getDelegatee() {
            checkActive(owner);
            return getBuilder();
        }

        @Override
        protected MiDBString getStringDelegatee() {
            return getBuilder();
        }
    }
    
    private static final AutocloseableBuilder NO_CLOSABLE = () -> {};
}
