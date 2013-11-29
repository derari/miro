package org.cthul.miro.query.parts;

public interface ValuesQueryPart extends SelectableQueryPart, FilterValueQueryPart {
    
    @Override
    Selector selector();
    
    interface Selector extends SelectableQueryPart.Selector, FilterValueQueryPart.Selector {
        
    }
}
