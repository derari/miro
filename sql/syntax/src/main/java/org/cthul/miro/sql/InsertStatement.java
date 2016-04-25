package org.cthul.miro.sql;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.stmt.MiUpdate;

/**
 *
 */
public interface InsertStatement extends InsertFromBuilder, InsertValuesBuilder, MiUpdate {
    
    @Override
    Into into();
    
    interface Into extends InsertFromBuilder.Into<Into>, InsertValuesBuilder.Into<Into> {
    }
    
    static InsertStatement create(MiConnection cnn) {
        return cnn.newStatement(SqlDQML.insert());
    }
}
