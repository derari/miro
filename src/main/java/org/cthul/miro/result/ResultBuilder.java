package org.cthul.miro.result;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Creates a result, made of entities from a {@code ResultSet}.
 * 
 * @param <Result>
 * @param <Entity> 
 */
public interface ResultBuilder<Result, Entity> {
    
    Result build(ResultSet rs, EntityType<Entity> type, EntityConfiguration<? super Entity> config) throws SQLException;
}
