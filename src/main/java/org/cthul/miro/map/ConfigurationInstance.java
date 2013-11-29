package org.cthul.miro.map;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.cthul.miro.MiConnection;
import org.cthul.miro.map.z.SimpleMapping;
import org.cthul.miro.result.EntityConfiguration;
import org.cthul.miro.result.EntityInitializer;

/**
 *
 */
public class ConfigurationInstance<Entity> implements ConfigurationProvider<Entity> {
    
    public static <Entity> ConfigurationProvider<Entity> asFactory(Object o) {
        if (o instanceof ConfigurationProvider) {
            return (ConfigurationProvider<Entity>) o;
        } else if (o instanceof EntityConfiguration) {
            return new ConfigurationInstance<>((EntityConfiguration<Entity>) o);
        } else if (o instanceof EntityInitializer) {
            return new ConfigurationInstance<>(new InitInstance<>((EntityInitializer<Entity>) o));
        } else {
            throw new IllegalArgumentException(String.valueOf(o));
        }
    }
    
    public static <Entity> EntityConfiguration<? super Entity> asConfiguration(Object o) {
        return asConfiguration(o, null, null);
    }
    
    public static <Entity> EntityConfiguration<? super Entity> asConfiguration(Object o, MiConnection cnn, Mapping<Entity> mapping) {
        if (o instanceof ConfigurationProvider) {
            return ((ConfigurationProvider<Entity>) o).getConfiguration(cnn, mapping);
        } else if (o instanceof EntityConfiguration) {
            return (EntityConfiguration<Entity>) o;
        } else if (o instanceof EntityInitializer) {
            return new InitInstance<>((EntityInitializer<Entity>) o);
        } else {
            throw new IllegalArgumentException(String.valueOf(o));
        }
    }
    
    public static <Entity> EntityInitializer<? super Entity> asInitializer(Object o, ResultSet rs) throws SQLException {
        return asInitializer(o, null, null, rs);
    }
    
    public static <Entity> EntityInitializer<? super Entity> asInitializer(Object o, MiConnection cnn, SimpleMapping<Entity> mapping, ResultSet rs) throws SQLException {
        if (o instanceof ConfigurationProvider) {
            return ((ConfigurationProvider<Entity>) o).getConfiguration(cnn, mapping).newInitializer(rs);
        } else if (o instanceof EntityConfiguration) {
            return ((EntityConfiguration<Entity>) o).newInitializer(rs);
        } else if (o instanceof EntityInitializer) {
            return (EntityInitializer<Entity>) o;
        } else {
            throw new IllegalArgumentException(String.valueOf(o));
        }
    }
    
    private final EntityConfiguration<Entity> config;

    public ConfigurationInstance(EntityConfiguration<Entity> config) {
        this.config = config;
    }

    @Override
    public <E extends Entity> EntityConfiguration<Entity> getConfiguration(MiConnection cnn, Mapping<E> mapping) {
        return config;
    }
    
    private static class InitInstance<Entity> implements EntityConfiguration<Entity> {
        private final EntityInitializer<Entity> init;

        public InitInstance(EntityInitializer<Entity> init) {
            this.init = init;
        }
        
        @Override
        public EntityInitializer<Entity> newInitializer(ResultSet rs) throws SQLException {
            return init;
        }
    }
}
