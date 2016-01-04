package org.cthul.miro.composer;

import java.util.function.Function;
import org.cthul.miro.composer.impl.AdaptedTemplate;

/**
 * Adds parts to a composer.
 * @param <Builder>
 */
public interface Template<Builder> {
    
    void addTo(Object key, InternalComposer<? extends Builder> query);
    
    default <Adaptee> Template<Adaptee> adapt(Function<? super Adaptee, ? extends Builder> adapter) {
        return AdaptedTemplate.adapt(this, adapter);
    }
}
