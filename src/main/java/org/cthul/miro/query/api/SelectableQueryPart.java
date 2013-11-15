package org.cthul.miro.query.api;

public interface SelectableQueryPart extends QueryPart {
    
    void selectAttribute(String attribute);
}
