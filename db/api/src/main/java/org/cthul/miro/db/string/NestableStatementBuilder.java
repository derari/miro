package org.cthul.miro.db.string;

import java.util.function.Function;
import org.cthul.miro.db.request.AutocloseableBuilder;
import org.cthul.miro.db.request.StatementBuilder;
import org.cthul.miro.db.syntax.ClauseType;

/**
 * A builder implementation with sub-clause support.
 */
public class NestableStatementBuilder implements AutocloseableBuilder, StatementBuilder {
    
    private Nested autocloseableBuilder = NO_NESTED;

    public NestableStatementBuilder() {
    }

    protected void checkActive(AutocloseableBuilder active) {
        if (active == null) {
            closeNestedClause();
        } else if (active != autocloseableBuilder) {
            throw new IllegalStateException(active + " is closed");
        }
    }

    @Override
    public void close() {
        closeNestedClause();
    }
    
    @SuppressWarnings("ConvertToTryWithResources")
    protected void closeNestedClause() {
        AutocloseableBuilder closable = autocloseableBuilder;
        autocloseableBuilder = NO_NESTED;
        closable.close();
    }
    
    protected String peekNestedClause() {
        return autocloseableBuilder == NO_NESTED ? 
                "" :
                autocloseableBuilder.toString();
    }

    @Override
    public <Clause> Clause begin(ClauseType<Clause> type) {
        return as(parent -> newNestedClause(parent, type));
    }
    
    @Override
    public <Clause> Clause as(Function<StatementBuilder, Clause> factory) {
        closeNestedClause();
        Nested nested = new Nested(this);
        Clause clause = factory.apply(nested);
        autocloseableBuilder = nested;
        if ((clause instanceof AutocloseableBuilder) && !nested.hasSubclause(clause)) {
            nested.owner = (AutocloseableBuilder) clause;
        }
        return clause;
    }
    
    protected <Clause> Clause newNestedClause(StatementBuilder parent, ClauseType<Clause> type) {
        return type.createDefaultClause(null, parent);
    }
    
    protected static class Nested extends NestableStatementBuilder {
        AutocloseableBuilder owner = NO_CLOSABLE;
        private final NestableStatementBuilder parent;
        boolean closed = false;

        public Nested(NestableStatementBuilder parent) {
            this.parent = parent;
        }
        
        protected boolean hasSubclause(Object subclause) {
            return owner == subclause || 
                    ((NestableStatementBuilder) this).autocloseableBuilder.hasSubclause(subclause);
        }

        @Override
        protected <Clause> Clause newNestedClause(StatementBuilder parent, ClauseType<Clause> type) {
            return this.parent.newNestedClause(parent, type);
        }

        @Override
        public <Clause> Clause as(Function<StatementBuilder, Clause> factory) {
            if (closed) {
                throw new IllegalStateException("closed");
            }
            return super.as(factory);
        }

        @Override
        public void close() {
            super.close();
            owner.close();
            closed = true;
            // owner.close may have created additional nested clauses
            closeNestedClause();
        }
    }
    
    private static final AutocloseableBuilder NO_CLOSABLE = () -> {};
    private static final Nested NO_NESTED = new Nested(null) {
        @Override
        protected boolean hasSubclause(Object subclause) {
            return false;
        }
        @Override
        public void close() {
        }
    };
}
