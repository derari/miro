package org.cthul.miro.query.sql;

import org.cthul.miro.query.parts.SelectableQueryPart;

public interface UnionQueryBuilder extends SelectableQueryPart {
    
    UnionQueryBuilder union(SelectableQueryPart part);
}
