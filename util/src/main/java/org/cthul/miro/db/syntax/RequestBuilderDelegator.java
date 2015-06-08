package org.cthul.miro.db.syntax;

import java.util.List;
import org.cthul.miro.db.syntax.RequestBuilder;

/**
 *
 * @param <This>
 */
public abstract class RequestBuilderDelegator<This extends RequestBuilder<This>> implements RequestBuilder<This> {

    protected abstract RequestBuilder<?> getDelegatee();
    
    protected RequestBuilder<?> getWriteDelegatee() {
        return getDelegatee();
    }
    
    protected RequestBuilder<?> getArgumentsDelegatee() {
        return getDelegatee();
    }
    
    protected RequestBuilder<?> getStringDelegatee() {
        return getDelegatee();
    }
    
    protected This _this() {
        return (This) this;
    }
    
    @Override
    public This append(String query) {
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
    public This clearArguments() {
        getArgumentsDelegatee().clearArguments();
        return _this();
    }

    @Override
    public This pushArgument(Object arg) {
        getArgumentsDelegatee().pushArgument(arg);
        return _this();
    }

    @Override
    public This pushArguments(Iterable<Object> args) {
        getArgumentsDelegatee().pushArguments(args);
        return _this();
    }

    @Override
    public This pushArguments(Object... args) {
        getArgumentsDelegatee().pushArguments(args);
        return _this();
    }

    @Override
    public This setArgument(int index, Object arg) {
        getArgumentsDelegatee().setArgument(index, arg);
        return _this();
    }

    @Override
    public String toString() {
        return getStringDelegatee().toString();
    }
}
