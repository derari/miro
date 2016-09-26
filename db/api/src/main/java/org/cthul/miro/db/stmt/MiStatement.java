package org.cthul.miro.db.stmt;

import org.cthul.miro.db.MiException;
import org.cthul.miro.futures.MiAction;
import org.cthul.miro.futures.MiFuture;
import org.cthul.miro.util.Closeables;

/**
 * A statement that can be executed against its database.
 * @param <Result>
 */
public interface MiStatement<Result> {
    
    Result execute() throws MiException;
    
    default Result _execute() {
        try {
            return execute();
        } catch (MiException e) {
            throw Closeables.unchecked(e);
        }
    }
    
    MiAction<Result> asAction();
    
    default MiFuture<Result> submit() {
        return asAction().submit();
    }
}
