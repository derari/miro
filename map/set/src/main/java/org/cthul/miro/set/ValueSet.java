package org.cthul.miro.set;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 *
 * @param <Entity>
 * @param <This>
 */
public class ValueSet<Entity, This extends ValueSet<Entity, This>> extends AbstractImmutable<This> {

    private final List<Entity> values = new ArrayList<>();
    
    public ValueSet() {
    }

    public ValueSet(ValueSet<Entity, This> source) {
        super(source);
        values.addAll(source.values);
    }
    
    public This values(Entity... values) {
        return values(Arrays.asList(values));
    }
    
    public This values(Collection<? extends Entity> values) {
        return doSafe(self -> ((ValueSet) self).values.addAll(values));
    }

    protected List<Entity> getValues() {
        return values;
    }
}
