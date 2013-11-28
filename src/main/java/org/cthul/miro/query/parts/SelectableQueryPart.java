package org.cthul.miro.query.parts;

public interface SelectableQueryPart extends QueryPart {
    
    void selectAttribute(String attribute, String alias);
}
