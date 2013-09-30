package org.cthul.miro.map;

import java.util.List;
import org.cthul.miro.MiConnection;
import org.cthul.miro.result.EntityConfiguration;

/**
 *
 */
public class EntityConfigInstance<Entity> implements EntityConfigFactory<Entity> {
    
    public static <Entity> EntityConfigFactory<Entity> asFactory(Object o) {
        if (o instanceof EntityConfigFactory) {
            return (EntityConfigFactory<Entity>) o;
        } else if (o instanceof EntityConfiguration) {
            return new EntityConfigInstance<>((EntityConfiguration<Entity>) o);
        } else {
            throw new IllegalArgumentException(String.valueOf(o));
        }
    }
    
    private final EntityConfiguration<Entity> config;

    public EntityConfigInstance(EntityConfiguration<Entity> config) {
        this.config = config;
    }
    
    @Override
    public EntityConfiguration<Entity> getConfiguration(MiConnection cnn, Mapping<? extends Entity> mapping, List<String> fields) {
        return config;
    }
}
