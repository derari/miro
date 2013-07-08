package org.cthul.miro.dsl;

import java.util.List;
import org.cthul.miro.MiConnection;
import org.cthul.miro.map.Mapping;
import org.cthul.miro.map.ResultBuilder;

/**
 *
 */
public interface ValueAdapterFactory<E> {

    ResultBuilder.ValueAdapter<E> newAdapter(Mapping<? extends E> mapping, MiConnection cnn, List<String> attributes);
    
}
