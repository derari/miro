package org.cthul.miro.query.syntax;

import java.util.List;
import org.cthul.miro.query.api.QueryPart;

public interface SqlBuilder {
    
    void addPart(QueryPart part);
    
    String getQueryString();
    
    List<Object> getArguments();
}
