package org.cthul.miro.map;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.cthul.miro.cursor.ResultCursor;

/**
 * Creates a result, made of entities from a {@code java.sql.ResultSet}.
 * 
 * @param <Result>
 * @param <Entity> 
 */
public interface ResultBuilder<Result, Entity> {

    /**
     * Creates a result
     * @param rs data source
     * @param ef used to create entities
     * @param va fills entities with data from {@code rs}
     * @return result
     * @throws SQLException 
     */
    Result adapt(ResultSet rs, EntityFactory<Entity> ef, ValueAdapter<? super Entity> va) throws SQLException;

    /**
     * Fills an entity with data from a {@code java.sql.ResultSet}.
     * <p>
     * Call Pattern:<br>
     * {@code ( initialize (apply* complete)* close )? }
     * @param <Entity> 
     */
    static interface ValueAdapter<Entity> extends AutoCloseable {

        void initialize(ResultSet rs) throws SQLException;

        void apply(Entity entity) throws SQLException;

        void complete() throws SQLException;
        
        @Override
        void close() throws SQLException;
    }

    /**
     * Creates new entities.
     * @param <Entity> 
     */
    static interface EntityFactory<Entity> extends AutoCloseable {

        void initialize(ResultSet rs) throws SQLException;

        Entity newEntity() throws SQLException;

        Entity newCursorValue(ResultCursor<Entity> rc) throws SQLException;

        Entity copy(Entity e) throws SQLException;
        
        @Override
        void close() throws SQLException;
    }
}
