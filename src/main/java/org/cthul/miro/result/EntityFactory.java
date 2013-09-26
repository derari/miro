package org.cthul.miro.result;

import java.sql.SQLException;
import org.cthul.miro.cursor.ResultCursor;

/**
 * Creates new entities.
 *
 * @param <Entity> 
 */
public interface EntityFactory<Entity> extends AutoCloseable {
    
    Entity newEntity() throws SQLException;

    Entity newCursorValue(ResultCursor<? super Entity> rc) throws SQLException;

    Entity copy(Entity e) throws SQLException;

    @Override
    void close() throws SQLException;
}
