package org.cthul.miro.query.api;

import java.util.List;

public interface QueryTemplatePart {
    
    List<String> getRequiredParts();
    
    QueryPart newQueryPart(QueryType queryType);
    
}
