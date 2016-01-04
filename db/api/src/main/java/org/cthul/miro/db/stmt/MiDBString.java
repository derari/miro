package org.cthul.miro.db.stmt;

/**
 * Builder for a database statement.
 */
public interface MiDBString {

    MiDBString append(CharSequence chars);
    
    MiDBString pushArgument(Object argument);
    
    default MiDBString pushArguments(Object... args) {
        MiDBString me = this;
        for (Object o: args) {
            me = me.pushArgument(o);
        }
        return me;
    }
    
    default MiDBString pushArguments(Iterable<?> args) {
        MiDBString me = this;
        for (Object o: args) {
            me = me.pushArgument(o);
        }
        return me;
    }
}
