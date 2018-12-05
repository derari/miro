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

    void set(Object... values);

    default void set(Collection<? extends Object> values) {
        batch(values.toArray());
    }

    @Override
    default void batch(Object... values) {
        set(values);
    }

    @Override
    default void batch(Collection<? extends Object> values) {
        set(values);
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
