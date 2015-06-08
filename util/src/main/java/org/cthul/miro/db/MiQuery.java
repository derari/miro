package org.cthul.miro.db;

import org.cthul.miro.futures.MiAction;
import org.cthul.miro.futures.MiFuture;

/**
 *
 */
public interface MiQuery {
    
    MiResultSet execute() throws MiException;
    
    MiAction<MiResultSet> asAction();
    
    default MiFuture<MiResultSet> submit() {
        return asAction().submit();
    }
}
