package org.cthul.miro.domain.impl;

import org.cthul.miro.entity.EntitySelector;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.request.MiQueryString;
import org.cthul.miro.db.request.MiRequest;
import org.cthul.miro.db.request.MiUpdateString;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.domain.*;
import org.cthul.miro.entity.*;

/**
 *
 */
public class SimpleRepository implements Repository, MiConnection {
    
    private final MiConnection connection;
    private final Map<Object, EntitySet<?>> setMap = new HashMap<>();
    private final Function<Object, EntityType<?>> typeLookUp;

    public SimpleRepository(MiConnection connection) {
        this(connection, o -> (EntityType) o);
    }
    
    public SimpleRepository(MiConnection connection, Function<Object, EntityType<?>> typeLookUp) {
        this.connection = connection;
        this.typeLookUp = typeLookUp;
    }
    
    @Override
    public <E> EntitySet<E> getEntitySet(Object key) {
        EntitySet<?> set = setMap.get(key);
        if (set == null) {
            EntityType<?> type = typeLookUp.apply(key);
            if (type == null) throw new IllegalArgumentException("" + key);
            set = newEntitySet(type);
            setMap.put(key, set);
        }
        return (EntitySet) set;
    }
    
    protected <E> EntitySet<E> newEntitySet(EntityType<E> type) {
        return new SimpleSet<>(type);
    }

    @Override
    public void close() throws MiException {
        
    }

    @Override
    public MiQueryString newQuery() {
        return connection.newQuery();
    }

    @Override
    public MiUpdateString newUpdate() {
        return connection.newUpdate();
    }

    @Override
    public <Req extends MiRequest<?>> Req newRequest(RequestType<Req> type) {
        return connection.newRequest(type);
    }
    
    protected class SimpleSet<E> implements EntitySet<E>, MappedSelector<E> {
        
        private final EntityType<E> type;
        private final MappedTemplate<E> lookUp;
        private final EntitySelector<E> newInstance;
        
        @SuppressWarnings("LeakingThisInConstructor")
        public SimpleSet(EntityType<E> type) {
            this.type = type;
            this.lookUp = type.newEntityLookUp(SimpleRepository.this, connection, this);
            this.newInstance = type.newEntityCreator(SimpleRepository.this);
        }

        @Override
        public MappedTemplate<E> getLookUp() {
            return lookUp;
        }

        @Override
        public MappedSelector<E> getSelector() {
            return this;
        }

        @Override
        public EntityConfiguration<E> readProperties(Collection<?> properties) {
            return type.getPropertyReader(SimpleRepository.this, properties);
        }

        @Override
        public void loadProperties(Collection<?> properties, InitializationBuilder<E> initBuilder) {
            type.newPropertyLoader(SimpleRepository.this, connection, properties, initBuilder);
        }

        @Override
        public E get(Object... key) throws MiException {
            return newInstance.get(key);
        }

        @Override
        public void complete() throws MiException {
            newInstance.complete();
        }

        @Override
        public String toString() {
            return "Set<" + type + ">";
        }

        @Override
        public MappedSelector<E> andLoad(Collection<?> properties) {
            return new MappedSelectorImpl<>(type, SimpleRepository.this, connection, this).andLoad(properties);
        }

        @Override
        public MappedSelector<E> andRead(Collection<?> properties) {
            return new MappedSelectorImpl<>(type, SimpleRepository.this, connection, this).andRead(properties);
        }
    }
}
