package org.cthul.miro.composer;

/**
 * Adds parts to a composer.
 * @param <Builder>
 */
public interface TemplatePart<Builder> {
    
    void addTo(Object key, InternalComposer<? extends Builder> query);
//    
//    default <Adaptee> TemplatePart<Adaptee> adapt(Function<? super Adaptee, ? extends Builder> adapter) {
//        return AdaptedTemplate.adapt(this, adapter);
//    }
}
