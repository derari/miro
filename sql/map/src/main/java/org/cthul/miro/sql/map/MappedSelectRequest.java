package org.cthul.miro.sql.map;

import org.cthul.miro.map.MappedQueryRequest;
import org.cthul.miro.sql.SelectQuery;

/**
 *
 * @param <Entity>
 */
public interface MappedSelectRequest<Entity> extends MappedSelectComposer<Entity>, MappedQueryRequest<Entity, SelectQuery> {
    
    @Override
    MappedSelectRequest<Entity> copy();
}
