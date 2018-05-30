package org.cthul.miro.composer.snippets;

import java.util.function.Consumer;
import org.cthul.miro.composer.node.Configurable;

/**
 *
 */
public interface Snippets<Builder> {

    void set(Object key, Object... args);
    
    void once(Object key, Consumer<? super Builder> action);
    
    default void once(Consumer<? super Builder> action) {
        once(action.getClass(), action);
    }
    
    void add(Consumer<? super Builder> action);
    
    Configurable get(Object key);
}
