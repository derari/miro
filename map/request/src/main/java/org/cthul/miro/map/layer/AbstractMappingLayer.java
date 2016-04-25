package org.cthul.miro.map.layer;

import org.cthul.miro.request.part.Copyable;
import org.cthul.miro.request.template.InternalComposer;
import org.cthul.miro.request.part.ListNode;
import org.cthul.miro.map.MappedType;
import org.cthul.miro.request.template.Template;
import org.cthul.miro.request.impl.AbstractTemplateLayer;
import org.cthul.miro.map.Mapping;
import org.cthul.miro.map.MappingKey;

/**
 *
 * @param <Entity>
 */
public abstract class AbstractMappingLayer<Entity, Builder> extends AbstractTemplateLayer<Builder> {

    private final MappedType<Entity,?> owner;

    public AbstractMappingLayer(MappedType<Entity,?> owner) {
        this.owner = owner;
    }

    protected MappedType<Entity,?> getOwner() {
        return owner;
    }

    @Override
    protected Template<? super Builder> createPartTemplate(Parent<Builder> parent, Object key) {
        return null;
    }
}
