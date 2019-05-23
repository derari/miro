package org.cthul.miro.map;

import org.cthul.miro.composer.RequestComposer;
import org.cthul.miro.db.request.MiQuery;

/**
 *
 * @param <Entity>
 * @param <Query>
 */
public interface MappedQueryRequest<Entity, Query extends MiQuery> extends MappedQueryComposer<Entity>, RequestComposer<MappedQuery<Entity, ? extends Query>> {

    @Override
    MappedQueryRequest<Entity, Query> copy();
}
