package org.cthul.miro.composer;

import org.cthul.miro.composer.impl.ValueKey;

/**
 * A query element that requires some arguments.
 */
public interface Configurable {

    default void enable() {
        set(Key.NO_VALUES);
    }

    void set(Object... values);
    
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

        public Key(String name, boolean unique) {
            super(name, unique);
        }
    }
}
