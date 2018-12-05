package org.cthul.miro.domain.impl;

import java.util.Collection;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.domain.EntityType;
import org.cthul.miro.domain.MappedTemplate;
import org.cthul.miro.domain.Repository;
import org.cthul.miro.entity.*;

/**
 *
 */
public class MappedTemplateImpl<Entity> implements MappedTemplate<Entity> {
    
    private final EntityType<Entity> type;
    private final Repository repository;
    private final MiConnection connection;
    private final EntityTemplate<? extends Entity> template;
    private final EntityConfiguration<Entity> configuration;

    public MappedTemplateImpl(EntityType<Entity> type, Repository repository, MiConnection connection, EntityTemplate<? extends Entity> template, EntityConfiguration<Entity> configuration) {
        this.type = type;
        this.repository = repository;
        this.connection = connection;
        this.template = template;
        this.configuration = configuration;
    }

    @Override
    public void newFactory(MiResultSet resultSet, FactoryBuilder<? super Entity> builder) throws MiException {
        builder.set(template, resultSet).add(configuration, resultSet);
    }

    @Override
    public MappedTemplate<Entity> andLoad(Collection<?> properties) {
        return new MappedTemplateImpl<>(type, repository, connection, this, (rs, b) -> type.newPropertyLoader(repository, connection, properties, b));
    }

    @Override
    public MappedTemplate<Entity> andRead(Collection<?> properties) {
        return new MappedTemplateImpl<>(type, repository, connection, this, type.getPropertyReader(repository, properties));
    }
}
