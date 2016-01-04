package org.cthul.miro.map.impl;

import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.map.MappedStatementBuilder;

/**
 *
 * @param <Entity>
 * @param <Statement>
 */
public class MappedStatement<Entity, Statement> 
                implements MappedStatementBuilder<Entity, Statement> {
    
    private EntityType<Entity> entityType;
    private final Statement statement;

    public MappedStatement(EntityType<Entity> entityType, Statement statement) {
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
    public Statement getStatement() {
        return statement;
    }
}
