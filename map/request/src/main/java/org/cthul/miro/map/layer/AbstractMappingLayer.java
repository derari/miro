package org.cthul.miro.map.layer;

import org.cthul.miro.map.MappedType;
import org.cthul.miro.request.impl.AbstractTemplateLayer;

/**
 *
 * @param <Entity>
 * @param <Builder>
 */
public abstract class AbstractMappingLayer<Entity, Builder> extends AbstractTemplateLayer<Builder> {

    private final MappedType<Entity,?> owner;

    public AbstractMappingLayer(MappedType<Entity,?> owner) {
        this.owner = owner;
    }

    protected MappedType<Entity,?> getOwner() {
        return owner;
    }
}
