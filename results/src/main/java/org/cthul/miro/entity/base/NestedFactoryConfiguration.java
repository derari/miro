package org.cthul.miro.entity.base;

import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityFactory;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;

/**
 *
 */
public abstract class NestedFactoryConfiguration<Outer, Inner> implements EntityConfiguration<Outer> {
    
    private final String shortString;

    public NestedFactoryConfiguration() {
        this.shortString = null;
    }

    public NestedFactoryConfiguration(String shortString) {
        this.shortString = shortString;
    }

    @Override
    public EntityInitializer<Outer> newInitializer(MiResultSet rs) throws MiException {
        return new NestedInitializer(nestedFactory(rs));
    }
    
    protected abstract EntityFactory<Inner> nestedFactory(MiResultSet rs) throws MiException;
    
    protected abstract void apply(Outer entity, EntityFactory<Inner> factory) throws MiException;

    @Override
    public String toString() {
        return getShortString();
    }
    
    protected String getShortString() {
        return shortString != null ? shortString : super.toString();
    }
    
    protected class NestedInitializer implements EntityInitializer<Outer> {

        private final EntityFactory<Inner> factory;

        public NestedInitializer(EntityFactory<Inner> factory) {
            this.factory = factory;
        }
        
        @Override
        public void apply(Outer entity) throws MiException {
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
            return factory + " -> " + getShortString();
        }
    }
}
