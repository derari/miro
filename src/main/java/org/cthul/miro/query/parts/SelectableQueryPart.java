package org.cthul.miro.query.parts;

public interface SelectableQueryPart extends QueryPart {
    
    Selector selector();
    
    interface Selector extends SqlQueryPart {
        
        void selectAttribute(String attribute, String alias);
    }
}
