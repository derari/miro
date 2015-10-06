package org.cthul.miro.composer;

/**
 *
 * @param <Builder>
 */
public interface QueryPart<Builder> {
    
    void addTo(Builder builder);

    default void setUp(Object... args) {
        if (args != null && args.length > 0) {
            throw new IllegalArgumentException(
                    this + ": no arguments expected");
        }
    }
    
    default void put(Object key, Object... args) {
        if (key == null) {
            setUp(args);
        } else {
            throw new IllegalArgumentException(
                    this + ": unsupported key " + key);
        }
    }
}
