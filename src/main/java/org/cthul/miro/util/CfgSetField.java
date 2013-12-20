package org.cthul.miro.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.cthul.miro.MiConnection;
import org.cthul.miro.map.ConfigurationProvider;
import org.cthul.miro.map.Mapping;
import org.cthul.miro.result.EntityConfiguration;
import org.cthul.miro.result.EntityInitializer;

public class CfgSetField implements ConfigurationProvider<Object> {
    
    private static final CfgSetField INSTANCE = new CfgSetField();
    
    public static CfgSetField getInstance() {
        return INSTANCE;
    }
    
    public static ConfigurationProvider<Object> newInstance(final String field, final Object value) {
        return new ConfigurationProvider<Object>() {
            @Override
            public <E> EntityConfiguration<? super E> getConfiguration(MiConnection cnn, Mapping<E> mapping, Object[] args) {
                return new Config<>(mapping, field, value);
            }
        };
    }

    public CfgSetField() {
    }
    
    @Override
    public <E extends Object> EntityConfiguration<E> getConfiguration(MiConnection cnn, Mapping<E> mapping, Object[] args) {
        String field = (String) args[0];
        Object value = (String) args[1];
        return new Config<>(mapping, field, value);
    }
    
    protected static class Config<Entity> implements EntityConfiguration<Entity>, EntityInitializer<Entity> {
        
        private final Mapping<Entity> mapping;
        private final String field;
        private final Object value;

        public Config(Mapping<Entity> mapping, String field, Object value) {
            this.mapping = mapping;
            this.field = field;
            this.value = value;
        }

        @Override
        public EntityInitializer<Entity> newInitializer(ResultSet rs) throws SQLException {
            return this;
        }

        @Override
        public void apply(Entity entity) throws SQLException {
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
