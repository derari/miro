package org.cthul.miro.map;

import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.Entities;
import org.cthul.miro.entity.EntityTemplate;

/**
 *
 * @param <Entity>
 * @param <Req>
 */
public class MappedStatement<Entity, Req> implements Mapping<Entity> {
    
    private final Req statement;
    private EntityTemplate<Entity> entityType = null;
    private EntityConfiguration<Entity> entityConfig = Entities.noConfiguration();

    public MappedStatement(Req statement) {
        this.statement = statement;
    }

    public Mapping<Entity> getMapping() {
        return this;
    }

    @Override
    public void setTemplate(EntityTemplate<Entity> type) {
        entityType = type;
    }

    @Override
    public void configureWith(EntityConfiguration<? super Entity> config) {
        entityConfig = entityConfig.and(config);
    }

    public EntityTemplate<Entity> getEntityType() {
        return entityType.with(entityConfig);
    }

    public Req getStatement() {
        return statement;
    }
}
