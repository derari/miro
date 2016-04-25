package org.cthul.miro.result.cursor;

import java.sql.SQLException;

/**
 *
 * @param <Entity>
 */
public interface CursorAdapter<Entity> {
        
    Entity newCursorValue(ResultCursor<? super Entity> rc) throws SQLException;

    Entity copy(Entity e) throws SQLException;
}
