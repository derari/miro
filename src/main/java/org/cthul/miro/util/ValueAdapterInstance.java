package org.cthul.miro.util;

import java.util.List;
import org.cthul.miro.MiConnection;
import org.cthul.miro.dsl.ValueAdapterFactory;
import org.cthul.miro.map.Mapping;
import org.cthul.miro.map.ResultBuilder;
import org.cthul.miro.map.ResultBuilder.ValueAdapter;

/**
 *
 */
public class ValueAdapterInstance<Entity> implements ValueAdapterFactory<Entity> {
    
    public static <Entity> ValueAdapterFactory<Entity> asFactory(ValueAdapter<Entity> va) {
        if (va instanceof ValueAdapterFactory) {
            return (ValueAdapterFactory<Entity>) va;
        } else {
            return new ValueAdapterInstance<>(va);
        }
    }
    
    private final ValueAdapter<Entity> va;

    public ValueAdapterInstance(ValueAdapter<Entity> va) {
        this.va = va;
    }

    @Override
    public ResultBuilder.ValueAdapter<Entity> newAdapter(Mapping<? extends Entity> mapping, MiConnection cnn, List<String> attributes) {
        return va;
    }
    
}
