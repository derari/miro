package org.cthul.miro.set.base;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.cthul.miro.result.Results;
import org.cthul.miro.set.ValueSet;

/**
 *
 * @param <Value>
 * @param <This>
 */
public abstract class AbstractValueSet<Value, This extends AbstractValueSet<Value, This>> implements ValueSet<Value> {
    
    private static final int NEW = 0, INITIALIZING = 1, READY = 2, FROZEN = 3;
    
    private Set<Object> onceGuard = null;
    private Constructor<This> copyConstructor = null;
    private int status;
    private int actionStack = 0;

    public AbstractValueSet() {
        this.status = NEW;
    }
    
    protected AbstractValueSet(AbstractValueSet<Value, This> source) {
        this.copyConstructor = source.copyConstructor;
        this.status = Math.min(source.status, READY);
        if (source.onceGuard != null) {
            this.onceGuard = new HashSet<>(source.onceGuard);
        }
    }
    
    protected This freeze() {
        if (status < READY) {
            throw new IllegalStateException("not initialized");
        }
        actionStack = 0;
        status = FROZEN;
        return (This) this;
    }
    
    protected boolean isFrozen() {
        return status == FROZEN;
    }
    
    protected void checkUnfrozen() {
        if (isFrozen()) {
            throw new IllegalStateException("Frozen");
        }
    }
    
    protected This copy() {
        if (copyConstructor == null) {
            for (Constructor<?> c: getClass().getDeclaredConstructors()) {
                Class<?>[] params = c.getParameterTypes();
                if (params.length == 1 && params[0].isAssignableFrom(getClass())) {
                    c.setAccessible(true);
                    copyConstructor = (Constructor) c;
                    break;
                }
            }
            if (copyConstructor == null) {
                throw new UnsupportedOperationException(
                        getClass() + " has no copy-constructor.");
            }
        }
        try {
            return copyConstructor.newInstance(this);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected This mutableCopy() {
        This copy = copy();
        ((AbstractValueSet) copy).actionStack = 1;
        return copy;
    }
    
    /**
     * Actions during initialize does not freeze set.
     * Copies of initialized sets will not initialize again.
     */
    protected void initialize() {
    }
    
    private void makeInitialized() {
        if (status >= READY) return;
        synchronized (this) {
            if (status >= INITIALIZING) return;
            status = INITIALIZING;
            try {
                actionStack++; 
                initialize();
            } finally {
                actionStack--;
                status = actionStack == 0 ? FROZEN : READY;
            }
        }
    }
    
    private This actualDo(Consumer<? super This> action) {
        checkUnfrozen();
        try {
            actionStack++;
            action.accept((This) this);
        } finally {
            actionStack--;
            if (actionStack == 0) freeze();
        }
        return (This) this;
    }
    
    protected This copyDo(Consumer<? super This> action) {
        makeInitialized();
        return initializedCopyDo(action);
    }
    
    private This initializedCopyDo(Consumer<? super This> action) {
        This copy = copy();
        ((AbstractValueSet) copy).actualDo(action);
        return copy;
    }
    
    protected This doSafe(Consumer<? super This> action) {
        makeInitialized();
        if (isFrozen()) {
            return initializedCopyDo(action);
        } else {
            return actualDo(action);
        }
    }
    
    protected This once(Consumer<? super This> action) {
        return once(action.getClass(), action);
    }
    
    protected This once(Object key, Consumer<? super This> action) {
        if (onceGuard != null && onceGuard.contains(key)) {
            return (This) this;
        }
        return doSafe(me -> {
            AbstractValueSet<?,?> vs = me;
            if (vs.onceGuard == null) {
                vs.onceGuard = new HashSet<>();
            }
            vs.onceGuard.add(key);
            action.accept(me);
        });
    }
    
    @Override
    public Results.Action<Value> result() {
        makeInitialized();
        return finish().buildResult();
    }
    
    protected This finish() {
        return (This) this;
    }

    protected abstract Results.Action<Value> buildResult();
}
