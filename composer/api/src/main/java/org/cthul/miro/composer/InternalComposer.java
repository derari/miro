package org.cthul.miro.composer;

import org.cthul.miro.composer.impl.AdaptedTemplate;
import java.util.function.Function;
import org.cthul.miro.util.Key;

/**
 * Internal API of a {@link Composer}.
 * @param <Builder>
 */
public interface InternalComposer<Builder> extends Composer {
    
    default <QP extends StatementPart<? super Builder>> void addPart(Key<? super QP> key, QP part) {
        addPart(part);
        addNode(key, part);
    }
    
    void addPart(StatementPart<? super Builder> part);
    
    <V> void addNode(Key<V> key, V node);
    
    default <Adapted> InternalComposer<Adapted> adapt(Function<? super Builder, ? extends Adapted> adapter) {
        return AdaptedTemplate.adapt(this, adapter);
    }
}
