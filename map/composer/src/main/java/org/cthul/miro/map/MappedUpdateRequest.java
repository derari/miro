package org.cthul.miro.map;

import org.cthul.miro.composer.RequestComposer;
import org.cthul.miro.db.request.MiUpdate;

/**
 *
 * @param <Entity>
 * @param <Query>
 */
public interface MappedUpdateRequest<Entity, Stmt extends MiUpdate> extends RequestComposer<Stmt> {

    @Override
    MappedUpdateRequest<Entity, Stmt> copy();
}
