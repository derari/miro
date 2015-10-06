package org.cthul.miro.dsl;

import java.util.List;
import org.cthul.miro.futures.MiAction;
import org.cthul.miro.result.EntityResultBuilder;
import org.cthul.miro.result.ResultBuilders;

/**
 * A query that that passes its result to an {@link EntityResultBuilder} to
 * build a result object.
 * @param <Entity> entity type
 */
public interface EntityQuery<Entity> extends ObjectQuery<List<Entity>> {
    
    <Result> ObjectQuery<Result> getResult(EntityResultBuilder<? extends Result, ? super Entity> resultBuilder);
    
    default ObjectQuery<List<Entity>> asList() {
        return getResult(ResultBuilders.getListResult());
    }
    
    default ObjectQuery<Entity> getFirst() {
        return getResult(ResultBuilders.getFirstResult());
    }
    
    default ObjectQuery<Entity> getSingle() {
        return getResult(ResultBuilders.getSingleResult());
    }

    @Override
    default MiAction<List<Entity>> asAction() {
        return asList().asAction();
    }
}
