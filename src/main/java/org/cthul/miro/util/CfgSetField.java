package org.cthul.miro.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.cthul.miro.MiConnection;
import org.cthul.miro.map.ConfigurationProvider;
import org.cthul.miro.map.Mapping;
import org.cthul.miro.result.EntityConfiguration;
import org.cthul.miro.result.EntityInitializer;

public class CfgSetField<Entity> implements ConfigurationProvider<Entity> {
    
    private final String field;
    private final Object value;

    public CfgSetField(String field, Object value) {
        this.field = field;
        this.value = value;
    }

    @Override
    public <E extends Entity> EntityConfiguration<E> getConfiguration(MiConnection cnn, Mapping<E> mapping) {
        return new Config<>(mapping);
    }
    
    protected class Config<E> implements EntityConfiguration<E>, EntityInitializer<E> {
        
        private final Mapping<E> mapping;

        public Config(Mapping<E> mapping) {
            this.mapping = mapping;
        }
        
        @Override
        public EntityInitializer<E> newInitializer(ResultSet rs) throws SQLException {
            return this;
        }

        @Override
        public void apply(E entity) throws SQLException {
            mapping.setField(entity, field, value);
        }

        @Override
        public void complete() throws SQLException {
        }

        @Override
        public void close() throws SQLException {
        }
    }
}
