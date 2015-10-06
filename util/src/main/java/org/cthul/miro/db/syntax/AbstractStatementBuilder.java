package org.cthul.miro.db.syntax;

/**
 *
 */
public abstract class AbstractStatementBuilder implements StatementBuilder, AutocloseableBuilder {
    
    private final Syntax syntax;
    private AutocloseableBuilder autocloseableBuilder = NO_CLOSABLE;

    public AbstractStatementBuilder(Syntax syntax) {
        this.syntax = syntax;
    }

    protected Syntax getSyntax() {
        return syntax;
    }
    
    protected abstract CoreStmtBuilder getBuilder();
    
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
        checkActive(null);
    }
    
    protected <Clause> Clause createClause(CoreStmtBuilder coreBuilder, ClauseType<Clause> type) {
        return getSyntax().newClause(coreBuilder, type);
    }
    
    @Override
    public <Clause> Clause begin(ClauseType<Clause> type) {
        checkActive(null);
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
    
    private class PublicCoreStmtBuilder extends CoreStmtBuilderDelegator {
        
        AutocloseableBuilder owner = null;

        @Override
        protected CoreStmtBuilder getDelegatee() {
            checkActive(owner);
            return getBuilder();
        }

        @Override
        protected CoreStmtBuilder getStringDelegatee() {
            return getBuilder();
        }
    }
    
    private static final AutocloseableBuilder NO_CLOSABLE = () -> {};
}
