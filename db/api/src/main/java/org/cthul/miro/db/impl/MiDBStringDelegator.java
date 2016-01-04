package org.cthul.miro.db.impl;

import org.cthul.miro.db.stmt.MiDBString;

/**
 * Wraps a {@link MiDBString}.
 */
public abstract class MiDBStringDelegator<This extends MiDBString> implements MiDBString {

    protected abstract MiDBString getDelegatee();
    
    protected MiDBString getWriteDelegatee() {
        return getDelegatee();
    }
    
    protected MiDBString getArgumentsDelegatee() {
        return getDelegatee();
    }
    
    protected MiDBString getStringDelegatee() {
        return getDelegatee();
    }

    @Override
    public This append(CharSequence chars) {
        getWriteDelegatee().append(chars);
        return (This) this;
    }

    @Override
    public This pushArgument(Object argument) {
        getArgumentsDelegatee().pushArgument(argument);
        return (This) this;
    }

    @Override
    public String toString() {
        return getStringDelegatee().toString();
    }
}