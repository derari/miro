package org.cthul.miro.futures;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class MiFutures {
    
    private static final MiFunction<Throwable, ?> EXPECTED_SUCCESS = new ExpectedSuccess<>();
    
    private static final MiFunction<?, ?> EXPECTED_FAIL = new ExpectedFail<>();
    
    private static final Executor RUN_NOW_EXECUTOR = Runnable::run;
    
    @SuppressWarnings("unchecked")
    public static <R> MiFunction<Throwable, R> expectedSuccess() {
        return (MiFunction) EXPECTED_SUCCESS;
    }
    
    @SuppressWarnings("unchecked")
    public static <V, R> MiFunction<V, R> expectedFail() {
        return (MiFunction) EXPECTED_FAIL;
    }
    
    public static <V, R> MiFunction<MiFuture<? extends V>, R> onComplete(MiFunction<? super V, ? extends R> onSuccess, MiFunction<? super Throwable, ? extends R> onFail) {
        return new OnComplete<>(onSuccess, onFail);
    }
    
    public static <V, R> MiFunction<MiFuture<? extends V>, R> onSuccess(MiFunction<? super V, ? extends R> onSuccess) {
        return new OnSuccess<>(onSuccess);
    }
    
    public static <V, R> MiFunction<MiFuture<? extends V>, R> onFailure(MiFunction<? super Throwable, ? extends R> onFail) {
        return new OnFailure<>(onFail);
    }
    
    @SuppressWarnings("ThrowableResultIgnored")
    public static RuntimeException rethrowUnchecked(Throwable e) {
        String message = null;
        if (e instanceof Error) {
            throw (Error) e;
        }
        if ((e instanceof ExecutionException) || 
                (e instanceof InvocationTargetException)) {
            message = e.getMessage();
            e = e.getCause();
        }
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        }
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        throw new RuntimeException(message, e);
    }
    
    public static Executor asExecutor(Consumer<? super Runnable> c) {
        return c::accept;
    }
    
    public static Executor runNowExecutor() {
        return RUN_NOW_EXECUTOR;
    }
    
    public static <T, R> MiFuture<R> submit(Executor executor, T arg, MiFunction<? super T, ? extends R> function) {
        return new MiAction<>(executor, arg, function).submit();
    }
    
    protected static class ExpectedSuccess<R> implements MiFunction<Throwable, R> {
        @Override
        public R call(Throwable arg) throws Throwable {
            throw new IllegalStateException(
                "Expected success, but operation failed", arg);
        }
    }
    
    protected static class ExpectedFail<T, R> implements MiFunction<T, R> {
        @Override
        public R call(T arg) throws Throwable {
            String s;
            try {
                s = String.valueOf(arg);
            } catch (Exception e) {
                s = "<" + e.getMessage() + ">";
            }
            throw new IllegalStateException(
                    "Expected fail, but operation returned " + s);
        }
    }
    
    protected static class OnComplete<T, R> implements MiFunction<MiFuture<? extends T>, R> {
        private final MiFunction<? super T, ? extends R> onSuccess;
        private final MiFunction<? super Throwable, ? extends R> onFail;

        public OnComplete(MiFunction<? super T, ? extends R> onSuccess, MiFunction<? super Throwable, ? extends R> onFail) {
            this.onSuccess = onSuccess;
            this.onFail = onFail;
        }
        
        @Override
        public R call(MiFuture<? extends T> f) throws Throwable {
            if (f.hasResult()) {
                return onSuccess.call(f.getResult());
            } else {
                return onFail.call(f.getException());
            }
        }
    }
    
    protected static class OnSuccess<T, R> extends OnComplete<T, R> {
        public OnSuccess(MiFunction<? super T, ? extends R> onSuccess) {
            super(onSuccess, expectedSuccess());
        }
    }
    
    protected static class OnFailure<T, R> extends OnComplete<T, R> {
        public OnFailure(MiFunction<? super Throwable, ? extends R> onFail) {
            super(expectedFail(), onFail);
        }
    }
}
