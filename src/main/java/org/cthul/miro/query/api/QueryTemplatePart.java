package org.cthul.miro.query.api;

import org.cthul.miro.query.parts.QueryPart;
import java.util.List;

public interface QueryTemplatePart {
    
    List<String> getRequiredParts();
    
    QueryPart newQueryPart(QueryType queryType);
    
}
