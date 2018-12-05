package org.cthul.miro.db.impl;

import org.cthul.miro.db.request.MiDBString;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.Syntax;

/**
 * Wraps a {@link QlBuilder}.
 * @param <This>
 */
public abstract class QlBuilderDelegator<This extends QlBuilder<This>> 
                extends AbstractStatementBuilder
                implements QlBuilder<This> {
    
    public QlBuilderDelegator(Syntax syntax) {
        super(syntax);
    }

    @Override
    protected MiDBString getBuilderForNestedClause() {
        return getWriteDelegate();
    }
    
    protected abstract QlBuilder<?> getDelegate();

    protected QlBuilder<?> getWriteDelegate() {
        closeNestedClause();
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
    public String toString() {
        return getStringDelegate().toString();
    }
}
