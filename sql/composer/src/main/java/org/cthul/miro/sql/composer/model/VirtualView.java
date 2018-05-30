package org.cthul.miro.sql.composer.model;

import org.cthul.miro.composer.node.Configurable;
import org.cthul.miro.composer.node.MapNode;

/**
 *
 */
public interface VirtualView extends MapNode<String, Configurable> {
    
    void addSnippet(String key);
    
    /**
     * Returns a {@link SqlAttribute} and adds all its dependencies to the query.
     * @param key
     * @return attribute
     */
    SqlAttribute getAttribute(String key);
}
