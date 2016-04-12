package org.cthul.miro.db.sql;

import org.cthul.miro.db.syntax.QlBuilder;

/**
 *
 */
public interface InsertFromBuilder {
    
    Into into();
    
    SelectBuilder query();
    
    interface Into<This extends Into<This>> extends QlBuilder<This>, SelectBuilder {
    }
}
