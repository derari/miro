package org.cthul.miro.db;

import org.cthul.miro.futures.MiAction;
import org.cthul.miro.futures.MiFuture;

/**
 *
 */
public interface MiStatement {
    
    Long execute() throws MiException;
    
    MiAction<Long> asAction();
    
    default MiFuture<Long> submit() {
        return asAction().submit();
    }
}
