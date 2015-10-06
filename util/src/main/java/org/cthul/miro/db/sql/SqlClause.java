package org.cthul.miro.db.sql;

import org.cthul.miro.db.syntax.QlBuilder;

/**
 *
 */
public interface SqlClause {
    
    interface Composite<This extends Composite<This>> extends SqlClause, QlBuilder<This> {
        
        This and();
    }
    
    interface Where<This extends Where<This>> extends Composite<This> {
    }
}
