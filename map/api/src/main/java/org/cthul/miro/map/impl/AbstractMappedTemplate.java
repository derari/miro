package org.cthul.miro.map.impl;

import org.cthul.miro.composer.Template;
import org.cthul.miro.composer.impl.AbstractTemplate;
import org.cthul.miro.map.MappedStatementBuilder;

/**
 *
 */
public abstract class AbstractMappedTemplate<Entity, Stmt> extends AbstractTemplate<MappedStatementBuilder<Entity, Stmt>> {

    private final MappingTemplates<Entity> owner;

    public AbstractMappedTemplate(MappingTemplates<Entity> owner, Template<? super MappedStatementBuilder<Entity, Stmt>> parent) {
        super(parent);
        this.owner = owner;
    }

    protected MappingTemplates<Entity> getOwner() {
        return owner;
    }

    @Override
    protected Template<? super MappedStatementBuilder<Entity, Stmt>> createPartType(Object key) {
        return null;
    }
}
