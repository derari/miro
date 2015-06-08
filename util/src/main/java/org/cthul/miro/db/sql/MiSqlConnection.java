package org.cthul.miro.db.sql;

import org.cthul.miro.db.MiConnection;

/**
 *
 */
public interface MiSqlConnection extends MiConnection {
    
    SqlQuery newSqlQuery();
}
