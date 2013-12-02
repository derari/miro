package org.cthul.miro.result;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Creates {@linkplain EntityFactory entity factories}.
 * 
 * @param <Entity>
 */
public interface EntityType<Entity> {
    
    EntityFactory<Entity> newFactory(ResultSet rs) throws SQLException;
    
    Entity[] newArray(int length);
}
