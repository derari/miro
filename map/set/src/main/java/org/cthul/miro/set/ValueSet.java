package org.cthul.miro.set;

import java.util.Iterator;
import org.cthul.miro.result.Results;

/**
 *
 * @param <Value>
 */
public interface ValueSet<Value> extends Iterable<Value> {
    
    Results.Action<Value> result();
    
    @Override
    default Iterator<Value> iterator() {
        return result()._asList().iterator();
    }
}
