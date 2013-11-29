package org.cthul.miro.map;

import org.cthul.miro.query.template.DataQueryTemplateProvider;

public class MappedDataQueryTemplateProvider<Entity>
                extends DataQueryTemplateProvider 
                implements MappedTemplateProvider<Entity> {

    private final Mapping<Entity> mapping;

    public MappedDataQueryTemplateProvider(Mapping<Entity> mapping) {
        this.mapping = mapping;
    }

    @Override
    public Mapping<Entity> getMapping() {
        return mapping;
    }
    
}
