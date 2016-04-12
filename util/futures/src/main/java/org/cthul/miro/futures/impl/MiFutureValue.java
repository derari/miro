package org.cthul.miro.futures.impl;

import java.util.concurrent.Executor;
import org.cthul.miro.futures.MiFutureResult;

/**
 *
 * @param <V>
 */
public class MiFutureValue<V> extends AbstractMiFuture<V> implements MiFutureResult<V> {

    public MiFutureValue() {
        this(true);
    }

    public MiFutureValue(boolean resettable) {
        super(resettable);
    }

    public MiFutureValue(Executor defaultExecutor) {
        super(defaultExecutor, true);
    }

    public MiFutureValue(Executor defaultExecutor, boolean resettable) {
        super(defaultExecutor, resettable);
    }
    
    protected void modify() {
        if (isDone()) {
            fastReset();
        }
    }
    
    @Override
    public void setResult(V value) {
        modify();
        result(start(), value);
    }
    
    @Override
    public void setFail(Throwable throwable) {
        modify();
        fail(start(), throwable);
    }

    @Override
    public boolean continueWork() {
        return !isDone();
    }

    @Override
    public MiFutureValue<V> reset() {
        fastReset();
        return this;
    }
}
