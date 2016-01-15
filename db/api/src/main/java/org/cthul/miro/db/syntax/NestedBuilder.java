package org.cthul.miro.db.syntax;

/**
 *
 */
public interface NestedBuilder<Owner> extends AutocloseableBuilder {
    
    Owner end();
}
