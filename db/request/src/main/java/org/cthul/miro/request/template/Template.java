package org.cthul.miro.request.template;

import org.cthul.miro.util.Key;

/**
 * Adds parts to a composer.
 * 
 * @param <Builder>
 */
public interface Template<Builder> {
    
    void addTo(Key<?> key, InternalComposer<? extends Builder> composer);
}
