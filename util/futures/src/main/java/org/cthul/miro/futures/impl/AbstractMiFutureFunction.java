package org.cthul.miro.futures.impl;

import java.util.concurrent.Executor;
import org.cthul.miro.function.MiFunction;

public abstract class AbstractMiFutureFunction<T, R> extends AbstractMiSubmittable<R> {

    private final MiFunction<? super T, ? extends R> function;

    public AbstractMiFutureFunction(MiFunction<? super T, ? extends R> function, boolean resettable) {
        super(resettable);
        this.function = function;
    }

    public AbstractMiFutureFunction(MiFunction<? super T, ? extends R> function, Executor executor, boolean resettable) {
        super(executor, resettable);
        this.function = function;
    }

    public AbstractMiFutureFunction(MiFunction<? super T, ? extends R> function, Executor executor, Executor defaultExecutor, boolean resettable) {
        super(executor, defaultExecutor, resettable);
        this.function = function;
    }

    protected AbstractMiFutureFunction<T, R> run(T arg) {
        run(new Runner(arg));
        return this;
    }
    
    protected AbstractMiFutureFunction<T, R> submit(T arg) {
        submit(new Runner(arg));
        return this;
    }
    
    protected class Runner extends AbstractMiSubmittable.Runner {
        private final T arg;

        public Runner(T arg) {
            this.arg = arg;
        }

        @Override
        protected void call(long attempt) throws Throwable {
            R result = function.call(arg);
            result(result);
        }
    }
}
