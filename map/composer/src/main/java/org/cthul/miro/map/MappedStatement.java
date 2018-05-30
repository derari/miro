package org.cthul.miro.map;

import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.entity.EntityTypes;

/**
 *
 * @param <Entity>
 * @param <Stmt>
 */
public class MappedStatement<Entity, Stmt> implements Mapping<Entity> {
    
    private final Stmt statement;
    private EntityType<Entity> entityType = null;
    private EntityConfiguration<Entity> entityConfig = EntityTypes.noConfiguration();

    public MappedStatement(Stmt statement) {
        this.statement = statement;
    }

    public Mapping<Entity> getMapping() {
        return this;
    }

    @Override
    public void setType(EntityType<Entity> type) {
        entityType = type;
    }

    @Override
    public void configureWith(EntityConfiguration<? super Entity> config) {
        entityConfig = entityConfig.and(config);
    }

    public EntityType<Entity> getEntityType() {
        return entityType.with(entityConfig);
    }

    public Stmt getStatement() {
        return statement;
    }
}
