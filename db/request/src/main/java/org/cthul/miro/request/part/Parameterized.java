package org.cthul.miro.request.part;

import java.util.Collection;
import org.cthul.miro.request.impl.ValueKey;

/**
 * A query element that requires some arguments.
 */
public interface Parameterized extends BatchNode<Object> {

    default void enable() {
        batch(Key.NO_VALUES);
    }

    @Override
    default void batch(Object... values) {
        set(values);
    }

    @Override
    default void batch(Collection<? extends Object> values) {
        set(values);
    }

    void set(Object... values);

    default void set(Collection<? extends Object> values) {
        set(values.toArray());
    }
    
    static Key key(Object value) {
        return new Key(value);
    }
    
    static Key uniqueKey(String name) {
        return new Key(name, true);
    }
    
    class Key extends ValueKey<Parameterized> {
        
        private static final Object[] NO_VALUES = {};

        public Key(Object value) {
            super(value);
        }

        public Key(Object name, boolean unique) {
            super(name, unique);
        }
    }
}
