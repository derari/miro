package org.cthul.miro.util;

import java.util.Arrays;

/**
 *
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
    
    public static <E extends Exception> void closeAll(Class<E> exType, Iterable<? extends AutoCloseable> closeables) throws E {
        Exception exception = null;
        for (AutoCloseable ac: closeables) {
            try {
                ac.close();
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
        for (AutoCloseable c: closeables) {
            try {
                c.close();
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
                ac.complete();
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
    
    @SuppressWarnings("unchecked")
    public static <T extends Throwable> RuntimeException exceptionAs(Throwable t, Class<T> throwableClass) throws T {
        if (throwableClass.isInstance(t)) {
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
    
}
