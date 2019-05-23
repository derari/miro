package org.cthul.miro.db.syntax;

import org.cthul.miro.db.request.StatementBuilder;
import org.cthul.miro.db.string.MiDBString;
import org.cthul.miro.db.string.NestableStatementBuilder;

/**
 * Wraps a {@link QlBuilder}.
 * @param <This>
 */
public abstract class QlBuilderDelegator<This extends QlBuilder<This>> 
                extends NestableStatementBuilder
                implements QlBuilder<This> {
    
    private final Syntax syntax;
    
    public QlBuilderDelegator(Syntax syntax) {
        this.syntax = syntax;
    }

    protected abstract QlBuilder<?> getDelegate();

    protected QlBuilder<?> getWriteDelegate() {
        closeNestedClause();
        return getNestedWriteDelegate();
    }

    protected QlBuilder<?> getNestedWriteDelegate() {
        return getDelegate();
    }
    
    protected QlBuilder<?> getArgumentsDelegate() {
        closeNestedClause();
        return getDelegate();
    }
    
    protected QlBuilder<?> getStringDelegate() {
        return getDelegate();
    }
    
    protected This _this() {
        return (This) this;
    }
    
    @Override
    public This append(CharSequence query) {
        getWriteDelegate().append(query);
        return _this();
    }

    @Override
    public This ql(String query) {
        getWriteDelegate().ql(query);
        return _this();
    }

    @Override
    public This identifier(String id) {
        getWriteDelegate().identifier(id);
        return _this();
    }

    @Override
    public This id(String id) {
        getWriteDelegate().id(id);
        return _this();
    }

    @Override
    public This namedTable(String table, String name) {
        getWriteDelegate().namedTable(table, name);
        return _this();
    }

    @Override
    public This attribute(String id, String attribute) {
        getWriteDelegate().attribute(id, attribute);
        return _this();
    }

    @Override
    public This stringLiteral(String string) {
        getWriteDelegate().stringLiteral(string);
        return _this();
    }

    @Override
    public This constant(Object key) {
        getWriteDelegate().constant(key);
        return _this();
    }

    @Override
    public This pushArgument(Object arg) {
        getArgumentsDelegate().pushArgument(arg);
        return _this();
    }

    @Override
    public This pushArguments(Iterable<?> args) {
        getArgumentsDelegate().pushArguments(args);
        return _this();
    }

    @Override
    public This pushArguments(Object... args) {
        getArgumentsDelegate().pushArguments(args);
        return _this();
    }

    @Override
    protected <Clause> Clause newNestedClause(StatementBuilder parent, ClauseType<Clause> type) {
        if (type == MiDBString.TYPE) {
            return getNestedWriteDelegate().begin(type);
        }
        return syntax.newClause(parent, type);
    }
    
    @Override
    public String toString() {
        return getStringDelegate().toString();
    }
}
