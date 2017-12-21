package org.cthul.miro.futures;

import org.cthul.miro.function.MiSupplier;
import org.cthul.miro.function.MiFunction;
import org.cthul.miro.futures.impl.MiFutureDelegator;
import org.cthul.miro.futures.impl.MiSubmittableAction;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.cthul.miro.function.MiConsumer;
import org.cthul.miro.futures.impl.MiFinal;
import org.cthul.miro.futures.impl.MiFutureValue;
import org.cthul.miro.futures.impl.MiTriggerableAction;

public class MiFutures {
    
    public static Builder build() {
        return new Builder();
    }
    
    private static final MiFunction EXPECTED_SUCCESS = new ExpectedSuccess<>();
    
    private static final MiFunction EXPECTED_FAIL = new ExpectedFail<>();
    
    public static <R> MiFunction<Throwable, R> expectedSuccess() {
        return EXPECTED_SUCCESS;
    }
    
    public static <V, R> MiFunction<V, R> expectedFail() {
        return EXPECTED_FAIL;
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
        String message = e.getMessage();
        if (e instanceof Error) {
            throw (Error) e;
        }
        if ((e instanceof ExecutionException) || 
                (e instanceof InvocationTargetException)) {
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
        return POOL;
    }
    
    public static <V> MiFuture<V> value(V value) {
        return new MiFinal<>(value);
    }
    
    public static <R> MiResettableAction<R> action(MiSupplier<? extends R> action) {
        return new MiSubmittableAction<>(action, true);
    }
    
    public static <R> MiResettableAction<R> action(Executor executor, MiSupplier<? extends R> action) {
        return new MiSubmittableAction<>(action, executor, true);
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
        Runnable r = () -> {};
        return new FutureAsAction<>(r, r, future, null);
    }
    
    public static <V> MiAction<V> futureAsAction(MiFuture<V> future, Executor defaultExecutor, Runnable submitAction) {
        return futureAsAction(future, defaultExecutor, submitAction, submitAction);
    }
    
    public static <V> MiAction<V> futureAsAction(MiFuture<V> future, Executor defaultExecutor, Runnable submitAction, Runnable runAction) {
        return new FutureAsAction<>(submitAction, runAction, future, defaultExecutor);
    }
    
    private static final ThreadPoolExecutor POOL;
    
    static {
        BlockingQueue<Runnable> bq = new LinkedBlockingQueue<>();
        ThreadFactory tf = new ThreadFactory() {
            AtomicInteger count = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "MiWorker-" + count.incrementAndGet());
            }
        };
        POOL = new ThreadPoolExecutor(3, 20, 15, TimeUnit.SECONDS, bq, tf);
    }
    
    protected static class ExpectedSuccess<R> implements MiFunction<Throwable, R> {
        @Override
        public R call(Throwable arg) throws Throwable {
            throw arg;
        }
    }
    
    protected static class ExpectedFail<T, R> implements MiFunction<T, R> {
        @Override
        public R call(T arg) throws Throwable {
            String s;
            try {
                s = String.valueOf(arg);
            } catch (Exception e) {
                s = arg.getClass().getSimpleName() + "<" + e.getMessage() + ">";
            }
            throw new IllegalArgumentException(
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
    
    private static class FutureAsAction<V> extends MiFutureDelegator<V> implements MiAction<V> {
        
        private final Runnable submitAction;
        private final Runnable runAction;
        private MiFuture<V> trigger = null;

        public FutureAsAction(Runnable submitAction, Runnable runAction, MiFuture<? extends V> delegate, Executor defaultExecutor) {
            super(delegate, defaultExecutor);
            Objects.requireNonNull(submitAction, "submit action");
            Objects.requireNonNull(runAction, "run action");
            this.submitAction = submitAction;
            this.runAction = runAction;
        }

        @Override
        public MiFuture<V> getTrigger() {
            class Trigger extends MiFutureDelegator<V> {
                public Trigger() {
                    super(FutureAsAction.this);
                }
                @Override
                public void await() throws InterruptedException {
                    runAction.run();
                    super.await();
                }
                @Override
                protected MiFuture<V> getDelegate() {
                    FutureAsAction.this.submit();
                    return super.getDelegate();
                }
                @Override
                protected MiFuture<V> getCancelDelegate() {
                    return super.getDelegate();
                }
            }
            if (trigger == null) {
                trigger = new Trigger();
            }
            return trigger;
        }

        @Override
        public MiFuture<V> submit() {
            submitAction.run();
            return this;
        }

        @Override
        public <R> MiAction<R> onComplete(Executor executor, MiFunction<? super MiFuture<V>, ? extends R> function) {
            MiFuture<R> f = getDelegate().onComplete(executor, function);
            return futureAsAction(f, executor, submitAction, runAction);
        }

        @Override
        public <R> MiAction<R> onCompleteAlways(Executor executor, MiFunction<? super MiFuture<V>, ? extends R> function) {
            MiFuture<R> f = getDelegate().onCompleteAlways(executor, function);
            return futureAsAction(f, executor, submitAction, runAction);
        }
    }
    
    public static class Builder {
        protected Boolean resettable = null;
        protected Executor executor = null;
        protected Executor defExecutor = null;

        public Builder resettable(boolean resettable) {
            this.resettable = resettable;
            return this;
        }
        
        public Builder resettable() {
            return resettable(true);
        }
        
        public Builder notResettable() {
            return resettable(false);
        }
        
        public Builder executor(Executor executor) {
            this.executor = executor;
            return this;
        }

        public Builder defaultExecutor(Executor executor) {
            this.defExecutor = executor;
            return this;
        }

        protected boolean isResettable() {
            return resettable == null ? true : resettable;
        }

        protected Executor getExecutor() {
            return executor;
        }

        protected Executor getDefaultExecutor() {
            if (defExecutor != null) return defExecutor;
            return executor;
        }
        
        protected Executor getSingleExecutor() {
            if (executor != null && defExecutor != null && executor != defExecutor) {
                throw new IllegalArgumentException("Action executor not supported");
            }
            return getDefaultExecutor();
        }
        
        protected void resettableNotSupported() {
            if (Boolean.TRUE.equals(resettable)) {
                throw new IllegalArgumentException("Reset not supported");
            }
        }
        
        public <V> MiFuture<V> value(V value) {
            resettableNotSupported();
            return new MiFinal<>(getSingleExecutor(), value);
        }
        
        public <V> MiFuture<V> throwable(Throwable throwable) {
            resettableNotSupported();
            return new MiFinal<>(getSingleExecutor(), throwable);
        }
        
        public <V> MiFuture<V> cancelled() {
            resettableNotSupported();
            return new MiFinal<>(getSingleExecutor());
        }
        
        public <V> MiFutureResult<V> futureValue() {
            return new MiFutureValue<>(getSingleExecutor(), isResettable());
        }
        
        public <V> MiResettableAction<V> action(MiSupplier<? extends V> action) {
            return new MiSubmittableAction<>(action, getExecutor(), getDefaultExecutor(), isResettable());
        }
        
        public <A, V> MiResettableAction<V> action(MiFunction<? super A, ? extends V> function, A arg) {
            return action(function.curry(arg));
        }
        
        public <V> MiActionResult<V> onTrigger(MiConsumer<? super MiResult<V>> action) {
            return new MiTriggerableAction<>(action, getExecutor(), getDefaultExecutor(), isResettable());
        }
    }
}
