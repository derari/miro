package org.cthul.miro.query.api;

import java.util.List;
import org.cthul.miro.query.syntax.QueryType;

public interface QueryPartTemplate {
    
    List<String> getRequiredParts();
    
    QueryPart newQueryPart(QueryType queryType);
    
}
