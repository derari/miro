package org.cthul.miro.db.impl;

import org.cthul.miro.db.stmt.MiDBString;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.Syntax;

/**
 * Foundation for implementing a {@link QlBuilder} 
 * on top of a {@link MiDBString}.
 * @param <This>
 */
public abstract class AbstractQlBuilder<This extends AbstractQlBuilder<This>> extends AbstractStatementBuilder implements QlBuilder<This> {

    private final MiDBString coreBuilder;

    public AbstractQlBuilder(Syntax syntax, MiDBString coreBuilder) {
        super(syntax);
        this.coreBuilder = coreBuilder;
    }

    @Override
    protected MiDBString getBuilder() {
        return coreBuilder;
    }

    @Override
    public This append(CharSequence query) {
        closeNestedClause();
        coreBuilder.append(query);
        return (This) this;
    }

    @Override
    public This pushArgument(Object arg) {
        closeNestedClause();
        coreBuilder.pushArgument(arg);
        return (This) this;
    }
}
