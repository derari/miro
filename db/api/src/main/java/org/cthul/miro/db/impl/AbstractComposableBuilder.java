package org.cthul.miro.db.impl;

import org.cthul.miro.db.syntax.AutocloseableBuilder;
import java.util.function.Function;
import org.cthul.miro.db.stmt.MiDBString;

/**
 * A builder implementation with sub-clause support.
 */
public abstract class AbstractComposableBuilder implements AutocloseableBuilder {
    
    private AutocloseableBuilder autocloseableBuilder = NO_CLOSABLE;

    public AbstractComposableBuilder() {
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
    
    protected void closeNestedClause() {
        autocloseableBuilder.close();
        autocloseableBuilder = NO_CLOSABLE;
    }
    
    /**
     * Returns a string builder for nested clauses to write to.
     * Should not return this object, for writing on this will close
     * nested clauses.
     * @return db string
     */
    protected abstract MiDBString getBuilderForNestedClause();

    protected <Clause> Clause newNestedClause(Function<MiDBString, Clause> factory) {
        closeNestedClause();
        MiDBString dBString = getBuilderForNestedClause();
        NestedDBString nestedString = new NestedDBString(dBString);
        Clause clause = factory.apply(nestedString);
        if (clause instanceof AutocloseableBuilder) {
            autocloseableBuilder = (AutocloseableBuilder) clause;
            nestedString.owner = autocloseableBuilder;
        }
        return clause;
    }
    
    @Override
    public String toString() {
        return getBuilderForNestedClause().toString();
    }
    
    protected class NestedDBString extends MiDBStringDelegator<MiDBString> {
        
        final MiDBString dbString;
        AutocloseableBuilder owner = null;

        public NestedDBString(MiDBString dbString) {
            this.dbString = dbString;
        }

        @Override
        protected MiDBString getBuilderForNestedClause() {
            return dbString;
        }

        protected MiDBString getInternalDbString() {
            return dbString;
        }

        @Override
        protected void checkActive(AutocloseableBuilder active) {
            super.checkActive(active);
            AbstractComposableBuilder.this.checkActive(owner);
        }
        
        @Override
        protected MiDBString getDelegatee() {
            AbstractComposableBuilder.this.checkActive(owner);
            return getInternalDbString();
        }

        @Override
        protected MiDBString getStringDelegatee() {
            return getInternalDbString();
        }
    }
    
    private static final AutocloseableBuilder NO_CLOSABLE = () -> {};
}
