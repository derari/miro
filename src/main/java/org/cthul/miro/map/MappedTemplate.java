package org.cthul.miro.map;

import org.cthul.miro.query.template.QueryTemplate;

public interface MappedTemplate<Entity> extends QueryTemplate {
    
    Mapping<Entity> getMapping();
    
}
