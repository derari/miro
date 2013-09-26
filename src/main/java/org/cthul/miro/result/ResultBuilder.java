package org.cthul.miro.result;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Creates a result, made of entities from a {@code java.sql.ResultSet}.
 * 
 * @param <Result>
 * @param <Entity> 
 */
public interface ResultBuilder<Result, Entity> {
    
    Result build(ResultSet rs, EntityType<Entity> type, EntitySetup<? super Entity> setup) throws SQLException;
}
