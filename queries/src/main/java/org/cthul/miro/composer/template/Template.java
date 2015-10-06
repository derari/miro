package org.cthul.miro.composer.template;

import java.util.function.Function;

/**
 *
 * @param <Builder>
 */
public interface Template<Builder> {
    
    void addTo(Object key, InternalQueryComposer<? extends Builder> query);
    
    default <Adaptee> Template<Adaptee> adapt(Function<? super Adaptee, ? extends Builder> adapter) {
        return AdaptedTemplate.adapt(this, adapter);
    }
}
