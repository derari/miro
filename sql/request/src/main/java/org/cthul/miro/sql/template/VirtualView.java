package org.cthul.miro.sql.template;

import org.cthul.miro.request.part.Configurable;
import org.cthul.miro.request.part.MapNode;

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
