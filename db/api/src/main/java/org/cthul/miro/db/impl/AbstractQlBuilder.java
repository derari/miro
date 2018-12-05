package org.cthul.miro.db.impl;

import org.cthul.miro.db.request.MiDBString;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.Syntax;

/**
 * Foundation for implementing a {@link QlBuilder} 
 * on top of a {@link MiDBString}.
 * @param <This>
 */
public abstract class AbstractQlBuilder<This extends QlBuilder<This>> extends AbstractStatementBuilder implements QlBuilder<This> {

    private final MiDBString dbString;

    public AbstractQlBuilder(Syntax syntax, MiDBString dbString) {
        super(syntax);
        this.dbString = dbString;
    }

    @Override
    protected MiDBString getBuilderForNestedClause() {
        return dbString;
    }
    
    protected MiDBString getWriteDelegate() {
        closeNestedClause();
        return dbString;
    }

    @Override
    public This append(CharSequence query) {
        getWriteDelegate().append(query);
        return (This) this;
    }

    @Override
    public This constant(Object key) {
        getSyntax().appendConstant(key, this);
        return (This) this;
    }

    @Override
    public This pushArgument(Object arg) {
        getWriteDelegate().pushArgument(arg);
        return (This) this;
    }
}
