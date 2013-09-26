package org.cthul.miro.result;

import java.sql.SQLException;

/**
 * Initializes entities.
 * 
 * @param <Entity> 
 */
public interface EntityInitializer<Entity> extends AutoCloseable {
    
    /**
     * Marks entity as to be initialized.
     * @param entity
     * @throws SQLException 
     */
    void apply(Entity entity) throws SQLException;

    /**
     * When this call returns, all entities previously passed to 
     * {@link #apply(java.lang.Object)} must be initialized.
     * @throws SQLException 
     */
    void complete() throws SQLException;

    @Override
    void close() throws SQLException;
}
