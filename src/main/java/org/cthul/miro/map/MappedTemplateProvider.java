package org.cthul.miro.map;

import org.cthul.miro.query.template.QueryTemplateProvider;

public interface MappedTemplateProvider<Entity> extends QueryTemplateProvider {
    
    Mapping<Entity> getMapping();
}
