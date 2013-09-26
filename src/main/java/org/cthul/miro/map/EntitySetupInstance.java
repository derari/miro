package org.cthul.miro.map;

import java.util.List;
import org.cthul.miro.MiConnection;
import org.cthul.miro.result.EntitySetup;

/**
 *
 */
public class EntitySetupInstance<Entity> implements EntitySetupFactory<Entity> {
    
    public static <Entity> EntitySetupFactory<Entity> asFactory(Object o) {
        if (o instanceof EntitySetupFactory) {
            return (EntitySetupFactory<Entity>) o;
        } else if(o instanceof EntitySetup) {
            return new EntitySetupInstance<>((EntitySetup<Entity>) o);
        } else {
            throw new IllegalArgumentException(String.valueOf(o));
        }
    }
    
    private final EntitySetup<Entity> setup;

    public EntitySetupInstance(EntitySetup<Entity> setup) {
        this.setup = setup;
    }
    
    @Override
    public EntitySetup<Entity> getSetup(MiConnection cnn, Mapping<? extends Entity> mapping, List<String> fields) {
        return setup;
    }
}
