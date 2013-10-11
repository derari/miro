package org.cthul.miro.at;

import java.util.List;
import org.cthul.miro.query.QueryBuilder;

/**
 * Provides methods of {@link QueryBuilder}
 */
public interface AnnotatedQueryBuilder {
    
    String getQueryString();
    
    List<Object> getArguments();
}
