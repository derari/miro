package org.cthul.miro.composer.node;

import java.util.Collection;
import org.cthul.miro.util.ValueKey;

/**
 * A query element that requires some arguments.
 */
public interface Configurable extends BatchNode<Object> {

    default void enable() {
        set(Key.NO_VALUES);
    }

    @Override
    void set(Object... values);

    @Override
    public default void set(Collection<? extends Object> values) {
        set(values.toArray());
    }
    
    static Key key(Object value) {
        return new Key(value);
    }
    
    static Key uniqueKey(String name) {
        return new Key(name, true);
    }
    
    class Key extends ValueKey<Configurable> {
        
        private static final Object[] NO_VALUES = {};

        public Key(Object value) {
            super(value);
        }

        public Key(Object name, boolean unique) {
            super(name, unique);
        }
    }
}
