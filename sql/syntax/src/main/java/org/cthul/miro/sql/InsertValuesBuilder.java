package org.cthul.miro.sql;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;
import org.cthul.miro.db.syntax.QlBuilder;

/**
 *
 */
public interface InsertValuesBuilder {
    
    Into into();
    
    Columns columns();
    
    Values values();
    
    default Values values(Object... values) {
        return values().add(values);
    }
    
    default Values values(Iterable<?> values) {
        return values().add(values);
    }
    
    interface Into<This extends Into<This>> extends QlBuilder<This>, InsertValuesBuilder {   
    }
    
    interface Columns extends QlBuilder<Columns>, InsertValuesBuilder {
        
        Columns and();
    }
    
    interface Values extends InsertValuesBuilder {
        
        default Values add(Object... values) {
            return add(Arrays.asList(values));
        }
        
        Values add(Iterable<?> values);
        
        default Values and() {
            return this;
        }
        
        default Values and(Object... values) {
            return and().add(values);
        }
        
        default Values and(Iterable<?> values) {
            return and().add(values);
        }
        
        default Values all(Iterable<?>... values) {
            return all(Arrays.asList(values));
        }
        
        default Values all(Iterable<? extends Iterable<?>> values) {
            return all(values.iterator());
        }
        
        default Values all(Stream<? extends Iterable<?>> values) {
            return all(values.iterator());
        }
        
        default Values all(Iterator<? extends Iterable<?>> values) {
            Values me = this;
            while (values.hasNext()) {
                me = me.add(values.next());
            }
            return me;
        }
    }
}
