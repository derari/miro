package org.cthul.miro.request.template;

import java.util.function.Consumer;
import org.cthul.miro.request.impl.SnippetTemplateLayer;

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
    
    class Key<Builder> implements org.cthul.miro.util.Key<Snippets<Builder>> {
        private static final Key INSTANCE = new Key();
    }
    
    static <Builder> Key<Builder> key() {
        return Key.INSTANCE;
    }
    
    static <Builder> SnippetTemplateLayer<Builder> newLayer() {
        return new SnippetTemplateLayer<>();
    }
    
    public static interface Snippet<Builder> {
        
        void accept(Builder builder, Object[] args);
        
        default Consumer<Builder> curry(Object[] args) {
            return builder -> accept(builder, args);
        }
    }
}
