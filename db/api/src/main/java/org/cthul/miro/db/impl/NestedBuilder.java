package org.cthul.miro.db.impl;

/**
 *
 */
public interface NestedBuilder<Owner> extends AutocloseableBuilder {
    
    Owner end();
}
