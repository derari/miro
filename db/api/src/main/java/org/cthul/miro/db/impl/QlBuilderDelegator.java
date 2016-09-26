package org.cthul.miro.db.impl;

import org.cthul.miro.db.stmt.MiDBString;
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
        return getWriteDelegatee();
    }
    
    protected abstract QlBuilder<?> getDelegatee();

    protected QlBuilder<?> getWriteDelegatee() {
        closeNestedClause();
        return getDelegatee();
    }
    
    protected QlBuilder<?> getArgumentsDelegatee() {
        closeNestedClause();
        return getDelegatee();
    }
    
    protected QlBuilder<?> getStringDelegatee() {
        return getDelegatee();
    }
    
    protected This _this() {
        return (This) this;
    }
    
    @Override
    public This append(CharSequence query) {
        getWriteDelegatee().append(query);
        return _this();
    }

    @Override
    public This ql(String query) {
        getWriteDelegatee().ql(query);
        return _this();
    }

    @Override
    public This identifier(String id) {
        getWriteDelegatee().identifier(id);
        return _this();
    }

    @Override
    public This id(String id) {
        getWriteDelegatee().id(id);
        return _this();
    }

    @Override
    public This namedTable(String table, String name) {
        getWriteDelegatee().namedTable(table, name);
        return _this();
    }

    @Override
    public This attribute(String id, String attribute) {
        getWriteDelegatee().attribute(id, attribute);
        return _this();
    }

    @Override
    public This stringLiteral(String string) {
        getWriteDelegatee().stringLiteral(string);
        return _this();
    }

    @Override
    public This constant(Object key) {
        getWriteDelegatee().constant(key);
        return _this();
    }

    @Override
    public This pushArgument(Object arg) {
        getArgumentsDelegatee().pushArgument(arg);
        return _this();
    }

    @Override
    public This pushArguments(Iterable<?> args) {
        getArgumentsDelegatee().pushArguments(args);
        return _this();
    }

    @Override
    public This pushArguments(Object... args) {
        getArgumentsDelegatee().pushArguments(args);
        return _this();
    }
    
    @Override
    public String toString() {
        return getStringDelegatee().toString();
    }
}
