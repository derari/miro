package org.cthul.miro.sql.template;

import org.cthul.miro.request.part.MapNode;
import org.cthul.miro.request.part.Parameterized;

/**
 *
 */
public interface ViewComposer extends MapNode<String, Parameterized> {
    
    void addSnippet(SnippetKey key);
    
    /**
     * Returns a {@link SqlAttribute} and adds all its dependencies to the query.
     * @param key
     * @return attribute
     */
    SqlAttribute getAttribute(String key);
}
