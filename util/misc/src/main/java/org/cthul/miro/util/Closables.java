package org.cthul.miro.util;

import java.util.Arrays;

/**
 * Helpful methods for closing or completing multiple objects at once,
 * with checked exception support.
 */
public class Closables {

    public static void closeUnchecked(AutoCloseable... closeables) {
        closeAll(RuntimeException.class, Arrays.asList(closeables));
    }

    public static void closeUnchecked(Iterable<AutoCloseable> closeables) {
        closeAll(RuntimeException.class, closeables);
    }
    
    public static void closeAll(AutoCloseable... closeables) throws Exception {
        closeAll(Arrays.asList(closeables));
    }
    
    public static void closeAll(Iterable<? extends AutoCloseable> closeables) throws Exception {
        closeAll(Exception.class, closeables);
    }
    
    public static <E extends Exception> void closeAll(Class<E> exType, AutoCloseable... closeables) throws E {
        closeAll(exType, Arrays.asList(closeables));
    }
    
    public static <E extends Exception> void closeAll(Class<E> exType, Iterable<? extends AutoCloseable> closeables) throws E {
        Exception exception = null;
        for (AutoCloseable ac: closeables) {
            try {
                if (ac != null) ac.close();
            } catch (Exception e) {
                if (exception == null) {
                    exception = e;
                } else {
                    exception.addSuppressed(e);
                }
            }
        }
        if (exception != null) {
            throw exceptionAs(exception, exType);
        }
    }
    
    public static <T extends Throwable> T closeAll(T throwable, AutoCloseable... closeables) {
        return closeAll(throwable, Arrays.asList(closeables));
    }
    
    public static <T extends Throwable> T closeAll(T throwable, Iterable<? extends AutoCloseable> closeables) {
        NullPointerException npe = null;
        Throwable container = throwable;
        if (container == null) {
            container = npe = new NullPointerException("throwable");
        }
        for (AutoCloseable ac: closeables) {
            try {
                if (ac != null) ac.close();
            } catch (Exception e) {
                container.addSuppressed(e);
            }
        }
        if (npe != null) {
            throw npe;
        }
        return throwable;
    }
    
    public static void completeUnchecked(Completable... completables) {
        completeUnchecked(Arrays.asList(completables));
    }
    
    public static void completeUnchecked(Iterable<? extends Completable> completables) {
        completeAll(RuntimeException.class, completables);
    }
    
    public static void completeAll(Completable... completables) throws Exception {
        completeAll(Arrays.asList(completables));
    }
    
    public static void completeAll(Iterable<? extends Completable> completables) throws Exception {
        completeAll(Exception.class, completables);
    }
    
    public static <E extends Exception> void completeAll(Class<E> exType, Completable... completables) throws E {
        completeAll(exType, Arrays.asList(completables));
    }
    
    public static <E extends Exception> void completeAll(Class<E> exType, Iterable<? extends Completable> completables) throws E {
        for (Completable ac: completables) {
            try {
                if (ac != null) ac.complete();
            } catch (Exception e) {
                throw exceptionAs(e, exType);
            }
        }
    }
    
// not needed, completion should not happen when a exception is floating
//    public static <T extends Throwable> T completeAll(T throwable, Completable... completables) {
//        return completeAll(throwable, Arrays.asList(completables));
//    }
//    
//    public static <T extends Throwable> T completeAll(T throwable, Iterable<? extends Completable> completables) {
//        NullPointerException npe = null;
//        Throwable container = throwable;
//        if (container == null) {
//            container = npe = new NullPointerException("throwable");
//        }
//        for (Completable c: completables) {
//            try {
//                c.complete();
//            } catch (Exception e) {
//                container.addSuppressed(e);
//                brea
//            }
//        }
//        if (npe != null) {
//            throw npe;
//        }
//        return throwable;
//    }
    
    public static RuntimeException unchecked(Throwable t) {
        return exceptionAs(t, RuntimeException.class);
    }
    
    /**
     * Throws throwable {@code t} if it is of type {@code T} or {@code Error},
     * otherwise returns a {@code RuntimeException}.
     * @param <T> expected throwable type
     * @param t the throwable
     * @param checkedEx expected throwable type
     * @return new runtime exception
     * @throws T if {@code t instanceof T}
     */
    @SuppressWarnings("unchecked")
    public static <T extends Throwable> RuntimeException exceptionAs(Throwable t, Class<T> checkedEx) throws T {
        if (checkedEx.isInstance(t)) {
            throw (T) t;
        }
        if (t instanceof Error) {
            throw (Error) t;
        }
        if (t instanceof RuntimeException) {
            return (RuntimeException) t;
        }
        return new RuntimeException(t);
    }
    
    public static void doUnchecked(XRunnable<?> runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            throw unchecked(e);
        }
    }
    
    public static <T> T doUnchecked(XSupplier<T,?> supplier) {
        try {
            return supplier.get();
        } catch (Throwable e) {
            throw unchecked(e);
        }
    }
    
    public static interface FunctionalHelper {
        default Object[] withLength(Object[] args, int len) {
            if (args == null || args.length != len) {
                return new Object[len];
            }
            return args;
        }
    }
}
