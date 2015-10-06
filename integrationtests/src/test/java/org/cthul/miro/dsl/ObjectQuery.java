package org.cthul.miro.dsl;

import java.util.concurrent.ExecutionException;
import org.cthul.miro.db.MiException;
import org.cthul.miro.futures.MiAction;
import org.cthul.miro.futures.MiFuture;
import org.cthul.miro.util.Closables;

/**
 * A query that returns a result object.
 * @param <Result> result type
 */
public interface ObjectQuery<Result> {
    
    MiAction<Result> asAction();
    
    default MiFuture<Result> submit() {
        return asAction().submit();
    }
    
    default Result execute() throws InterruptedException, MiException {
        try {
            return asAction().get();
        } catch (ExecutionException ex) {
            throw Closables.exceptionAs(ex.getCause(), MiException.class);
        }
    }
}
