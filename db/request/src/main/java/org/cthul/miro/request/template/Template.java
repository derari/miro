package org.cthul.miro.request.template;

/**
 * Adds parts to a composer.
 * 
 * @param <Builder>
 */
public interface Template<Builder> {
    
    void addTo(Object key, InternalComposer<? extends Builder> composer);
}
