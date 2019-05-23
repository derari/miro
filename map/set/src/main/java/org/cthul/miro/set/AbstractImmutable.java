package org.cthul.miro.set;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Base class for implementing immutables.
 * @param <This>
 */
public abstract class AbstractImmutable<This extends AbstractImmutable<This>> {
    
    private static final int NEW = 0, INITIALIZING = 1, READY = 2, FROZEN = 3;
    
    private Set<Object> onceGuard = null;
    private Function<This, This> copyConstructor = null;
    private int status;
    private int actionStack = 0;

    public AbstractImmutable() {
        this.status = NEW;
    }
    
    protected AbstractImmutable(AbstractImmutable<This> source) {
        this.copyConstructor = source.copyConstructor;
        this.status = Math.min(source.status, READY);
        if (source.onceGuard != null) {
            this.onceGuard = new HashSet<>(source.onceGuard);
        }
    }
    
    /**
     * Immediately freezes this object.
     * @return this
     * @throws IllegalStateException if initialization is not completed yet
     */
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
    
    /**
     * Asserts that this object is not frozen.
     * @throws IllegalStateException if this object is frozen
     */
    protected void checkUnfrozen() {
        if (isFrozen()) {
            throw new IllegalStateException("Frozen");
        }
    }
    
    /**
     * Returns an unfrozen copy of this object.
     * @return copy
     */
    protected This copy() {
        if (copyConstructor == null) {
            copyConstructor = findCopyConstructor();
        }
        return copyConstructor.apply((This) this);
    }
    
    protected Function<This, This> findCopyConstructor() {
        for (Constructor<?> c: getClass().getDeclaredConstructors()) {
            Class<?>[] params = c.getParameterTypes();
            if (params.length == 1 && params[0].isAssignableFrom(getClass())) {
                c.setAccessible(true);
                return self -> {
                    try {
                        return (This) c.newInstance(self);
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                };
            }
        }
        throw new UnsupportedOperationException(
                getClass() + " has no copy-constructor.");
    }
    
    /**
     * Returns a copy that remains mutable until {@link #freeze()} is called.
     * @return copy
     */
    protected This mutableCopy() {
        This copy = copy();
        ((AbstractImmutable) copy).actionStack = 1;
        return copy;
    }
    
    /**
     * Actions during initialize do not freeze set.
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
    
    /**
     * Invokes action with a copy of this and returns copy.
     * @param action
     * @return copy
     */
    protected This copyDo(Consumer<? super This> action) {
        makeInitialized();
        return initializedCopyDo(action);
    }
    
    private This initializedCopyDo(Consumer<? super This> action) {
        This copy = copy();
        ((AbstractImmutable) copy).actualDo(action);
        return copy;
    }
    
    /**
     * Invokes action with this, if unfrozen, or a copy.
     * @param action
     * @return this or copy
     */
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
            AbstractImmutable<?> vs = me;
            if (vs.onceGuard == null) {
                vs.onceGuard = new HashSet<>();
            }
            vs.onceGuard.add(key);
            action.accept(me);
        });
    }
}
