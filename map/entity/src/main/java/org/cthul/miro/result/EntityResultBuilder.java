package org.cthul.miro.result;

import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityTemplate;

/**
 * Creates a result, made of entities from a {@code ResultSet}.
 * 
 * @param <Result>
 * @param <Entity> 
 */
public interface EntityResultBuilder<Result, Entity> {
    
    Result build(MiResultSet rs, EntityTemplate<? extends Entity> type) throws MiException;
    
//    default Result build(EntityResult<Entity> result) throws MiException {
//        return result.buildWith(this);
//    }
//
//    @Override
//    default Result call(EntityResult<Entity> result) throws MiException {
//        return build(result);
//    }
//    
//    default MiFunction<MiResultSet, Result> asFunction(EntityTemplate<Entity> type) {
//        return rs -> build(rs, type);
//    }
}
