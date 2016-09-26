package org.cthul.miro.sql.template;

import org.cthul.miro.request.template.TemplateLayer;
import org.cthul.miro.sql.SelectBuilder;
import org.cthul.miro.util.Key;

/**
 *
 */
public interface JoinedView {
    
    String getPrefix();
    
    Key<ViewComposer> getViewKey();
    
    // TODO:
    // List<String> getKeyAttributes();
    
    void collectSelectTemplateLayers(Layers<SelectBuilder> bag);
    
    interface Layers<B> {
        
        boolean add(Key<?> key, TemplateLayer<? super B> layer);
    }
}
