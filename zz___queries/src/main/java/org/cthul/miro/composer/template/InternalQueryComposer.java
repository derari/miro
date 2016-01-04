package org.cthul.miro.composer.template;

import java.util.function.Function;
import org.cthul.miro.composer.QueryComposer;
import org.cthul.miro.composer.QueryPart;
import org.cthul.miro.util.Key;

/**
 *
 * @param <Builder>
 */
public interface InternalQueryComposer<Builder> extends QueryComposer {
    
    default <QP extends QueryPart<? super Builder>> void addPart(Key<QP> key, QP part) {
        addPart(part);
        addNode(key, part);
    }
    
    void addPart(QueryPart<? super Builder> part);
    
    <V> void addNode(Key<V> key, V node);
    
    default <Adapted> InternalQueryComposer<Adapted> adapt(Function<? super Builder, ? extends Adapted> adapter) {
        return AdaptedTemplate.adapt(this, adapter);
    }
}
