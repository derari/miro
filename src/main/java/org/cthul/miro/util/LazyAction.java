package org.cthul.miro.util;

import org.cthul.miro.MiConnection;
import org.cthul.miro.MiFuture;
import org.cthul.miro.MiFutureAction;

public class LazyAction<V> extends FutureDelegator<V> {

    private MiConnection cnn;
    private Object arg;
    private MiFutureAction<?, ? extends V> action;
    private MiFuture<V> value = null;    
    private Thread initThread = null;

    public LazyAction(MiConnection cnn, Object arg) {
        super(null);
        this.cnn = cnn;
        this.arg = arg;
        this.action = null;
    }
    
    public LazyAction(MiConnection cnn, Object arg, MiFutureAction<?, ? extends V> action) {
        super(null);
        this.cnn = cnn;
        this.arg = arg;
        this.action = action;
    }
    
    public boolean beDoneNow() {
        return initNow();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        synchronized (this) {
            if (value != null) {
                return value.cancel(mayInterruptIfRunning);
            }
            if (initThread != null && mayInterruptIfRunning) {
                initThread.interrupt();
                return true;
            }
        }
        return false;
    }
    
    public void reset() {
        synchronized (this) {
            cancel(true);
            isInitializing(); // wait for consisten state
            value = null;
        }
    }
    
    @Override
    protected MiFuture<V> getDelegatee() {
        if (value == null) {
            initValue();
        }
        return value;
    }
    
    private boolean initNow() {
        try {
            synchronized (this) {
                if (isInitializing()) return beDone();
                initThread = Thread.currentThread();
            }
            MiFutureAction<Object, ? extends V> a = (MiFutureAction) initialize();
            V result = a.call(arg);
            value = new FinalFuture<>(result);
        } catch(Exception e) {
            value = new FinalFuture<>(e);
        } finally {
            synchronized (this) {
                initThread = null;
                this.notifyAll(); // continue threads waiting in #isInitializing
            }
        }
        return true;
    }

    private synchronized void initValue() {
        if (isInitializing()) return;
        try {
            value = (MiFuture) initFuture();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        value.onComplete(new MiFutureAction<MiFuture<V>, Void>() {
            @Override
            public Void call(MiFuture<V> param) throws Exception {
                if (param.hasResult()) {
                    value = new FinalFuture<>(param.getResult());
                } else {
                    value = new FinalFuture<>(param.getException());
                }
                return null;
            }
        });
    }
    
    @SuppressWarnings("WaitWhileNotSynced")
    private boolean isInitializing() {
        try {
            while (initThread != null) {
                this.wait();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return true;
        }
        return value != null;
    }

    private MiFuture<V> initFuture() throws Exception {
        return cnn.submit((MiFutureAction<Object, V>)initialize(), arg);
    }
    
    protected MiFutureAction<?, ? extends V> initialize() throws Exception {
        return action;
    }
}
