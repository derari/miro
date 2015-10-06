package org.cthul.miro.db;

import org.cthul.miro.futures.MiAction;
import org.cthul.miro.futures.MiFuture;

/**
 *
 */
public interface MiStatement<Result> {
    
    Result execute() throws MiException;
    
    MiAction<Result> asAction();
    
    default MiFuture<Result> submit() {
        return asAction().submit();
    }
}
