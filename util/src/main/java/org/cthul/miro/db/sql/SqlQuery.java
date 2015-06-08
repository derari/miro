package org.cthul.miro.db.sql;

import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.futures.MiAction;
import org.cthul.miro.futures.MiFuture;

/**
 *
 */
public interface SqlQuery extends SqlClause{

    MiResultSet execute() throws MiException;
    
    MiAction<MiResultSet> asAction();
    
    default MiFuture<MiResultSet> submit() {
        return asAction().submit();
    }
}
