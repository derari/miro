package org.cthul.miro.db.syntax;

/**
 *
 */
public abstract class CoreStmtBuilderDelegator<This extends CoreStmtBuilder> implements CoreStmtBuilder {

    protected abstract CoreStmtBuilder getDelegatee();
    
    protected CoreStmtBuilder getWriteDelegatee() {
        return getDelegatee();
    }
    
    protected CoreStmtBuilder getArgumentsDelegatee() {
        return getDelegatee();
    }
    
    protected CoreStmtBuilder getStringDelegatee() {
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