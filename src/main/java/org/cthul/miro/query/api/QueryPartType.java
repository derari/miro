package org.cthul.miro.query.api;

import org.cthul.miro.query.adapter.QueryAdapter;
import org.cthul.miro.query.parts.QueryPart;

public interface QueryPartType<Builder> {
    
    void addPartTo(QueryPart part, QueryAdapter<? extends Builder> query);
}
