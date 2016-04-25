package org.cthul.miro.request.template;

import org.cthul.miro.request.Composer;
import org.cthul.miro.request.StatementPart;
import org.cthul.miro.util.Key;

/**
 * Internal API of a {@link Composer}.
 * @param <Builder>
 */
public interface InternalComposer<Builder> extends Composer {
    
    default <SP extends StatementPart<? super Builder>> void addPart(Key<? super SP> key, SP part) {
        addPart(part);
        addNode(key, part);
    }
    
    void addPart(StatementPart<? super Builder> part);
    
    <V> void addNode(Key<V> key, V node);
}
