package org.cthul.miro.query.sql;

import org.cthul.miro.query.parts.SelectableQueryPart;

public interface UnionStatement extends SelectableQueryPart {
    
    UnionStatement union(SelectableQueryPart part);
}
