package org.cthul.miro.sql.set;

import org.cthul.miro.map.MappedQuery;
import org.cthul.miro.request.RequestComposer2;
import org.cthul.miro.sql.SelectQuery;

/**
 *
 */
public interface MappedSelectRequest<Entity> extends MappedSelectComposer<Entity>, RequestComposer2<MappedQuery<Entity,SelectQuery>> {

    @Override
    MappedSelectRequest<Entity> copy();
}
