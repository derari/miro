package org.cthul.miro.db.syntax;

import org.cthul.miro.db.request.AutocloseableBuilder;

/**
 *
 */
public interface NestedBuilder<Owner> extends AutocloseableBuilder {
    
    Owner end();
}
