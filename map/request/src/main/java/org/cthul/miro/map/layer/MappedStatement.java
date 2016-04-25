package org.cthul.miro.map.layer;

import org.cthul.miro.request.StatementHolder;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.entity.EntityTypes;
import org.cthul.miro.map.Mapping;
import org.cthul.miro.map.MappingHolder;

/**
 *
 * @param <Entity>
 * @param <Stmt>
 */
public class MappedStatement<Entity, Stmt> 
                implements MappingHolder<Entity>, Mapping<Entity>, StatementHolder<Stmt> {
    
    private final Stmt statement;
    private EntityType<Entity> entityType = null;
    private EntityConfiguration<Entity> entityConfig = EntityTypes.noConfiguration();

    public MappedStatement(Stmt statement) {
        this.statement = statement;
    }

    @Override
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

    @Override
    public Stmt getStatement() {
        return statement;
    }
}
