package org.cthul.miro.composer;

/**
 *
 */
public interface RequestComposer<Builder> extends Composer {
    
    void build(Builder builder);
    
    RequestComposer<Builder> copy();
}
