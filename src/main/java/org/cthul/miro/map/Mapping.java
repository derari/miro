package org.cthul.miro.map;

import java.util.List;
import org.cthul.miro.result.EntityConfiguration;
import org.cthul.miro.result.EntityType;

/**
 *
 */
public interface Mapping<Entity> extends EntityType<Entity> {
    
    public void setField(Entity entity, String field, Object value);
    
    public Object getField(Entity entity, String field);
    
    public EntityConfiguration<Entity> newFieldConfiguration(List<String> fields);
    
    public Entity[] newArray(int length);
}
