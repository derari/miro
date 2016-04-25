package org.cthul.miro.request;

/**
 *
 * @param <Builder>
 */
public interface RequestComposer<Builder> extends Composer {
    
    void build(Builder builder);
    
    RequestComposer<Builder> copy();
}
