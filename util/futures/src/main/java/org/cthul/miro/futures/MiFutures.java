package org.cthul.miro.futures;

import org.cthul.miro.futures.impl.MiFutureDelegator;
import org.cthul.miro.futures.impl.SimpleMiAction;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Consumer;

public class MiFutures {
    
    private static final MiFunction<Throwable, ?> EXPECTED_SUCCESS = new ExpectedSuccess<>();
    
    private static final MiFunction<?, ?> EXPECTED_FAIL = new ExpectedFail<>();
    
//    private static final Executor RUN_NOW_EXECUTOR = Runnable::run;
    
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
    
    public static Executor defaultExecutor() {
        Executor ex = ForkJoinTask.getPool();
        if (ex != null) return ex;
        return ForkJoinPool.commonPool();
    }
    
    public static <R> MiAction<R> action(MiSupplier<? extends R> supplier) {
        return (MiAction) supplier.asAction();
    }
    
    public static <T, R> MiAction<R> action(Executor executor, T arg, MiFunction<? super T, ? extends R> function) {
        return new SimpleMiAction<>(executor, arg, function);
    }
    
    public static <T, R> MiFuture<R> submit(Executor executor, T arg, MiFunction<? super T, ? extends R> function) {
        return action(executor, arg, function).submit();
    }
    
    public static <V> MiFuture<V> trigger(MiAction<V> action) {
        return new MiFutureDelegator<V>(action) {
            @Override
            protected MiFuture<V> getDelegatee() {
                action.submit();
                return super.getDelegatee();
            }
            @Override
            protected MiFuture<V> getCancelDelegatee() {
                return super.getDelegatee();
            }
        };
    }
    
    public static <T> MiFunction<MiFuture<T>, T> reportException() {
        return reportException(t -> t.printStackTrace(System.err));
    }
    
    public static <T> MiFunction<MiFuture<T>, T> reportException(Consumer<Throwable> handler) {
        return f -> {
            if (f.hasFailed()) {
                Throwable t = f.getException();
                handler.accept(t);
                throw t;
            }
            return f.getResult();
        };
    }
    
    public static <V> MiAction<V> futureAsAction(MiFuture<V> future) {
        if (future instanceof MiAction) {
            return (MiAction<V>) future;
        }
        return new FutureAsAction<>(future, null);
    }
    
    public static <V> MiAction<V> futureAsAction(MiFuture<V> future, Executor defaultExecutor) {
        Runnable submit = null;
        if (future instanceof MiAction) {
            submit = ((MiAction) future)::submit;
        }
        return new FutureAsAction<>(submit, future, defaultExecutor);
    }
    
    public static <V> MiAction<V> futureAsAction(MiFuture<V> future, Runnable submit) {
        return new FutureAsAction<>(submit, future, null);
    }
    
    public static <V> MiAction<V> futureAsAction(MiFuture<V> future, Executor defaultExecutor, Runnable submit) {
        return new FutureAsAction<>(submit, future, defaultExecutor);
    }
    
    protected static class ExpectedSuccess<R> implements MiFunction<Throwable, R> {
        @Override
        public R call(Throwable arg) throws Throwable {
            throw arg;
//            throw new IllegalStateException(
//                "Expected success, but operation failed", arg);
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
    
    static class FutureAsAction<V> extends MiFutureDelegator<V> implements MiAction<V> {
        
        private final Runnable submitAction;
        private MiFuture<V> trigger = null;

        public FutureAsAction(MiFuture<? extends V> delegatee, Executor defaultExecutor) {
            this(null, delegatee, defaultExecutor);
        }

        public FutureAsAction(Runnable submitAction, MiFuture<? extends V> delegatee, Executor defaultExecutor) {
            super(delegatee, defaultExecutor);
            this.submitAction = submitAction;
            if (submitAction == null) {
                this.trigger = (MiFuture) delegatee;
            }
        }

        @Override
        public MiFuture<V> getTrigger() {
            if (trigger == null) {
                trigger = trigger(this);
            }
            return trigger;
        }

        @Override
        public MiFuture<V> submit() {
            if (submitAction != null) {
                submitAction.run();
            }
            return this;
        }

        @Override
        public <R> MiAction<R> onComplete(Executor executor, MiFunction<? super MiFuture<V>, ? extends R> function) {
            MiFuture<R> f = getDelegatee().onComplete(executor, function);
            return futureAsAction(f, executor, this::submit);
        }

        @Override
        public <R> MiAction<R> onCompleteAlways(Executor executor, MiFunction<? super MiFuture<V>, ? extends R> function) {
            MiFuture<R> f = getDelegatee().onCompleteAlways(executor, function);
            return futureAsAction(f, executor, this::submit);
        }
    }
}
