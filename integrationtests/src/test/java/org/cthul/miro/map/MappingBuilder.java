package org.cthul.miro.map;

import org.cthul.miro.composer.template.QueryPartType;
import org.cthul.miro.db.syntax.RequestType;

/**
 *
 * @author C5173086
 */
public class MappingBuilder {

    public static interface Entry<Builder> extends QueryPartType<Builder> {
        
        Object getKey();
        
        QueryPartType<Builder> templateForRequest(RequestType<?> requestType);
    }
    
}
