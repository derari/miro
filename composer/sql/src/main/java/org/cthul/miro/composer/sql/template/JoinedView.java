package org.cthul.miro.composer.sql.template;

import org.cthul.miro.composer.Configurable;
import org.cthul.miro.composer.MapNode;
import org.cthul.miro.composer.template.TemplateLayer;
import org.cthul.miro.db.sql.SelectBuilder;
import org.cthul.miro.util.Key;

/**
 *
 */
public interface JoinedView {
    
    String getPrefix();
    
    Key<MapNode<String, Configurable>> getSnippetKey();
    
    TemplateLayer<? super SelectBuilder> newSelectLayer();
    
//    MapNode<String, Configurable> newSelectSnippets(InternalComposer<?> ic);
}
