package org.cthul.miro.sql;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.request.MiQuery;

/**
 *
 */
public interface SelectQuery extends SelectBuilder, MiQuery {
    
    static SelectQuery create(MiConnection cnn) {
        return cnn.newRequest(SqlDQML.select());
    }
}
