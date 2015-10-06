package org.cthul.miro.composer.template;

import java.util.function.Function;
import org.cthul.miro.composer.QueryComposer;
import org.cthul.miro.composer.QueryPart;

/**
 *
 * @param <Builder>
 */
public interface InternalQueryComposer<Builder> extends QueryComposer {
    
    void addPart(Object key, QueryPart<? super Builder> part);
    
    default void addPart(QueryPart<? super Builder> part) {
        addPart(new Object(), part);
    }
    
    default <Adapted> InternalQueryComposer<Adapted> adapt(Function<? super Builder, ? extends Adapted> adapter) {
        return AdaptedTemplate.adapt(this, adapter);
    }
}
