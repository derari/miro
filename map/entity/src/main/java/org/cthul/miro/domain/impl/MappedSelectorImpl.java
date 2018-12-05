package org.cthul.miro.domain.impl;

import java.util.Collection;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.domain.EntityType;
import org.cthul.miro.domain.MappedSelector;
import org.cthul.miro.domain.Repository;
import org.cthul.miro.entity.Entities;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.entity.EntitySelector;
import org.cthul.miro.entity.builder.CompositeSelector;
import org.cthul.miro.util.XFunction;

/**
 *
 */
public class MappedSelectorImpl<Entity> extends CompositeSelector<Entity> implements MappedSelector<Entity> {

    private final EntityType<Entity> type;
    private final Repository repository;
    private final MiConnection connection;

    public MappedSelectorImpl(EntityType<Entity> type, Repository repository, MiConnection connection, EntitySelector<Entity> source) {
        super(source);
        this.type = type;
        this.repository = repository;
        this.connection = connection;
    }

    protected MappedSelectorImpl(EntityType<Entity> type, Repository repository, MiConnection connection, XFunction<Object[], ? extends Entity, ?> selector, Object factoryName, EntityInitializer<? super Entity> setup) {
        super(selector, factoryName, setup);
        this.type = type;
        this.repository = repository;
        this.connection = connection;
    }

    @Override
    public MappedSelector<Entity> andLoad(Collection<?> properties) {
        return new MappedSelectorImpl<>(type, repository, connection, selector, selectorName, 
                Entities.<Entity,RuntimeException>buildInitializer(b -> {
                    b.add(setup);
                    type.newPropertyLoader(repository, connection, properties, b);
                }));
    }

    @Override
    public MappedSelector<Entity> andRead(Collection<?> properties) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
}
