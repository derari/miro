package org.cthul.miro.query.parts;

import org.cthul.miro.query.api.QueryPart;

public interface SelectableQueryPart extends QueryPart {
    
    void selectAttribute(String attribute);
}
