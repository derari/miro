package org.cthul.miro.db.sql;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.stmt.MiQuery;

/**
 *
 */
public interface SelectQuery extends SelectBuilder, MiQuery {
    
    static SelectQuery create(MiConnection cnn) {
        return cnn.newStatement(SqlDQML.select());
    }
}
