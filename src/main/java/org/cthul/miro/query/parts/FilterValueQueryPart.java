package org.cthul.miro.query.parts;

import java.util.List;

public interface FilterValueQueryPart extends QueryPart {

    Selector selector();
    
    interface Selector extends QueryPart {
        
        void selectFilterValue(String key);
    
        void appendFilterValuesTo(List<Object> args);
    }
}
