package org.cthul.miro.map;

import org.cthul.miro.request.template.TemplateLayer;

/**
 *
 * @param <Entity>
 */
public interface MappingHolder<Entity> {

    Mapping<Entity> getMapping();
    
    static <Entity> TemplateLayer<MappingHolder<Entity>> wrapped(TemplateLayer<? super Mapping<Entity>> layer) {
        return layer.adapt(MappingHolder::getMapping);
    }
}
