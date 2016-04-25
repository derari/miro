package org.cthul.miro.entity.base;

import java.util.Objects;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityFactory;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;

/**
 * An entity configuration that applies values from an internal factory.
 * @param <Entity> entity type
 * @param <Inner> type of internal factory
 */
public abstract class NestedFactoryConfiguration<Entity, Inner> implements EntityConfiguration<Entity> {
    
    private final String shortString;

    public NestedFactoryConfiguration() {
        this.shortString = null;
    }

    public NestedFactoryConfiguration(Object shortString) {
        this.shortString = Objects.toString(shortString);
    }

    @Override
    public EntityInitializer<Entity> newInitializer(MiResultSet rs) throws MiException {
        return new NestedInitializer(nestedFactory(rs));
    }
    
    protected abstract EntityFactory<Inner> nestedFactory(MiResultSet rs) throws MiException;
    
    protected abstract void apply(Entity entity, EntityFactory<Inner> factory) throws MiException;

    @Override
    public String toString() {
        return getShortString();
    }
    
    protected String getShortString() {
        return shortString != null ? shortString : super.toString();
    }
    
    protected class NestedInitializer implements EntityInitializer<Entity> {

        private final EntityFactory<Inner> factory;

        public NestedInitializer(EntityFactory<Inner> factory) {
            this.factory = factory;
        }
        
        @Override
        public void apply(Entity entity) throws MiException {
            NestedFactoryConfiguration.this.apply(entity, factory);
        }

        @Override
        public void complete() throws MiException {
            factory.complete();
        }

        @Override
        public void close() throws MiException {
            factory.close();
        }

        @Override
        public String toString() {
            return "-> " + getShortString();
        }
    }
}
