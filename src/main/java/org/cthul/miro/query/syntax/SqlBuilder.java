package org.cthul.miro.query.syntax;

import java.util.List;
import org.cthul.miro.query.api.QueryPart;

public interface SqlBuilder {
    
    void addPart(QueryPart part);
    
    void buildQuery(StringBuilder sql, List<Object> args);
}
