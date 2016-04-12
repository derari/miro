package org.cthul.miro.composer.template;

import org.cthul.miro.composer.InternalComposer;

/**
 * Adds parts to a composer.
 * 
 * @param <Builder>
 */
public interface Template<Builder> {
    
    void addTo(Object key, InternalComposer<? extends Builder> composer);
}
