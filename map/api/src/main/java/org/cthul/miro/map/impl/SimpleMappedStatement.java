package org.cthul.miro.map.impl;

import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.map.MappedStatement;
import org.cthul.miro.map.Mapping;

/**
 *
 * @param <Entity>
 * @param <Builder>
 */
public class SimpleMappedStatement<Entity, Builder> 
                implements MappedStatement<Entity, Builder>, Mapping<Entity> {
    
    private EntityType<Entity> entityType;
    private final Builder statement;

    public SimpleMappedStatement(EntityType<Entity> entityType, Builder statement) {
        this.entityType = entityType;
        this.statement = statement;
    }

    @Override
    public Mapping getMapping() {
        return this;
    }

    @Override
    public void configureWith(EntityConfiguration<? super Entity> config) {
        entityType = entityType.with(config);
    }

    public EntityType<Entity> getEntityType() {
        return entityType;
    }

    @Override
    public Builder getStatement() {
        return statement;
    }
}
