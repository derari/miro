package org.cthul.miro.db.syntax;

/**
 * Foundation for implementing a {@link QlBuilder} 
 * on top of a {@link CoreStmtBuilder}.
 * @param <This>
 */
public abstract class AbstractQlBuilder<This extends AbstractQlBuilder<This>> extends AbstractStatementBuilder implements QlBuilder<This> {

    private final CoreStmtBuilder coreBuilder;

    public AbstractQlBuilder(Syntax syntax, CoreStmtBuilder coreBuilder) {
        super(syntax);
        this.coreBuilder = coreBuilder;
    }

    @Override
    protected CoreStmtBuilder getBuilder() {
        return this;
    }

    @Override
    public This append(CharSequence query) {
        coreBuilder.append(query);
        return (This) this;
    }

    @Override
    public This pushArgument(Object arg) {
        coreBuilder.pushArgument(arg);
        return (This) this;
    }

    @Override
    public String toString() {
        return coreBuilder.toString();
    }
}
