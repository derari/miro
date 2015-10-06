package org.cthul.miro.db.syntax;

/**
 *
 */
public interface CoreStmtBuilder {

    CoreStmtBuilder append(CharSequence chars);
    
    CoreStmtBuilder pushArgument(Object argument);
    
    default CoreStmtBuilder pushArguments(Object... args) {
        CoreStmtBuilder me = this;
        for (Object o: args) {
            me = me.pushArgument(o);
        }
        return me;
    }
    
    default CoreStmtBuilder pushArguments(Iterable<Object> args) {
        CoreStmtBuilder me = this;
        for (Object o: args) {
            me = me.pushArgument(o);
        }
        return me;
    }
}
