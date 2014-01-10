package org.cthul.miro.map;

import org.cthul.miro.query.InternalQueryBuilder;

public interface MappedInternalQueryBuilder extends InternalQueryBuilder {
    
    void configure(Object config);
    
    void configure(Object key, Object config);
}
