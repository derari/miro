package org.cthul.miro.map.impl;

import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.map.MappedBuilder;

/**
 *
 * @param <Entity>
 * @param <Builder>
 */
public class SimpleMappedBuilder<Entity, Builder> 
                implements MappedBuilder<Entity, Builder> {
    
    private EntityType<Entity> entityType;
    private final Builder statement;

    public SimpleMappedBuilder(EntityType<Entity> entityType, Builder statement) {
        this.entityType = entityType;
        this.statement = statement;
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
