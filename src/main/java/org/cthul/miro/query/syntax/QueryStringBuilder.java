package org.cthul.miro.query.syntax;

import java.util.List;
import org.cthul.miro.query.api.QueryAdapter;

public interface QueryStringBuilder extends QueryAdapter {
    
    String getQueryString();
    
    int getBatchCount();
    
    List<Object> getArguments(int batch);
}
