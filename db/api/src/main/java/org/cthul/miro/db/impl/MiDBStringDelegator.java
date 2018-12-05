package org.cthul.miro.db.impl;

import org.cthul.miro.db.request.MiDBString;

/**
 * Wraps a {@link MiDBString}.
 */
public abstract class MiDBStringDelegator<This extends MiDBString> extends AbstractComposableBuilder implements MiDBString {

    public MiDBStringDelegator() {
    }

    protected abstract MiDBString getDelegate();

    @Override
    protected MiDBString getBuilderForNestedClause() {
        return getDelegate();
    }
    
    protected MiDBString getWriteDelegate() {
        return getDelegate();
    }
    
    protected MiDBString getArgumentsDelegate() {
        return getDelegate();
    }
    
    protected MiDBString getStringDelegate() {
        return getDelegate();
    }

    @Override
    public This append(CharSequence chars) {
        getWriteDelegate().append(chars);
        return (This) this;
    }

    @Override
    public This pushArgument(Object argument) {
        getArgumentsDelegate().pushArgument(argument);
        return (This) this;
    }

    @Override
    public String toString() {
        return getStringDelegate().toString();
    }
}