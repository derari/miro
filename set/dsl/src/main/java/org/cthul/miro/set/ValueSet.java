package org.cthul.miro.set;

import java.util.Iterator;
import org.cthul.miro.result.Results;

/**
 *
 * @param <Value>
 */
public interface ValueSet<Value> extends Iterable<Value> {
    
//    List<Value> asList() throws MiException;
//    
//    default Value getSingle() throws MiException {
//        List<Value> list = asList();
//        if (list.size() != 1) {
//            throw new MiException(list.isEmpty() ? 
//                    "No results" : "Got " + list.size() + " results");
//        }
//        return list.get(0);
//    }
//    
//    default List<Value> _asList() {
//        try{
//            return asList();
//        } catch (MiException e) {
//            throw new RuntimeException(e);
//        }
//    }
//    
//    default Value _getSingle() {
//        try{
//            return getSingle();
//        } catch (MiException e) {
//            throw new RuntimeException(e);
//        }
//    }
//    
//    default boolean isEmpty() throws MiException {
//        return asList().isEmpty();
//    }

    Results.Action<Value> result();
    
    @Override
    default Iterator<Value> iterator() {
        return result()._asList().iterator();
    }
}
