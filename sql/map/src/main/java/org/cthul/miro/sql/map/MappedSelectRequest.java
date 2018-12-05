package org.cthul.miro.sql.map;

import org.cthul.miro.composer.RequestComposer;
import org.cthul.miro.map.MappedQuery;
import org.cthul.miro.sql.SelectQuery;

/**
 *
 * @param <Entity>
 */
public interface MappedSelectRequest<Entity> extends MappedSelectComposer<Entity>, RequestComposer<MappedQuery<Entity, SelectQuery>> {
    
    @Override
    MappedSelectRequest<Entity> copy();
}
