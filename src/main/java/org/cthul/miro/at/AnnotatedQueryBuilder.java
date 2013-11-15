package org.cthul.miro.at;

import java.util.List;
import org.cthul.miro.query.ZQueryBuilder;

/**
 * Provides methods of {@link ZQueryBuilder}
 */
public interface AnnotatedQueryBuilder {
    
    String getQueryString();
    
    List<Object> getArguments();
}
