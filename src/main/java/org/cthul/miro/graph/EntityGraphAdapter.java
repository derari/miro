package org.cthul.miro.graph;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.cthul.miro.result.EntityType;

public interface EntityGraphAdapter<Entity> extends EntityType<Entity> {
    
    KeyReader newKeyReader(ResultSet rs) throws SQLException;
    
    Object[] getKey(Entity e, Object[] array);
    
    interface KeyReader {
    
        Object[] getKey(Object[] array) throws SQLException;
    }
}
