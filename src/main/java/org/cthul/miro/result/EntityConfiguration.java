package org.cthul.miro.result;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Provides {@link EntityInitializer}s.
 * 
 * @param <Entity> 
 */
public interface EntityConfiguration<Entity> {
    
    EntityInitializer<Entity> newInitializer(ResultSet rs) throws SQLException;
}
