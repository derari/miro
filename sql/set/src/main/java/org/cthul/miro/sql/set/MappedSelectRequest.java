package org.cthul.miro.sql.set;

import org.cthul.miro.map.MappedQuery;
import org.cthul.miro.sql.SelectQuery;
import org.cthul.miro.composer.RequestComposer;

/**
 *
 */
public interface MappedSelectRequest<Entity> extends MappedSelectComposer<Entity>, RequestComposer<MappedQuery<Entity,SelectQuery>> {

    @Override
    MappedSelectRequest<Entity> copy();
}
