package org.cthul.miro.map.impl;

import org.cthul.miro.composer.template.Template;
import org.cthul.miro.composer.impl.AbstractTemplateLayer;
import org.cthul.miro.map.Mapping;

/**
 *
 */
public abstract class AbstractMappingLayer<Entity> extends AbstractTemplateLayer<Mapping<Entity>> {

    private final MappedTemplates<Entity> owner;

    public AbstractMappingLayer(MappedTemplates<Entity> owner) {
        this.owner = owner;
    }

    protected MappedTemplates<Entity> getOwner() {
        return owner;
    }

    @Override
    protected Template<? super Mapping<Entity>> createPartTemplate(Parent<Mapping<Entity>> parent, Object key) {
        return null;
    }
}
