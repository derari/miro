package org.cthul.miro.util;

/**
 *
 */
public class Closables {

    public static void uncheckedCloseAll(AutoCloseable... closeables) {
        try {
            closeAll(closeables);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void closeAll(AutoCloseable... closeables) throws Exception {
        Exception exception = null;
        for (int i = closeables.length -1; i >= 0; i--) {
            try {
                closeables[i].close();
            } catch (Exception e) {
                if (exception == null) {
                    exception = e;
                } else {
                    exception.addSuppressed(e);
                }
            }
        }
        if (exception != null) throw exception;
    }
    
    public static <T extends Throwable> T closeAll(T throwable, AutoCloseable... closeables) {
        NullPointerException npe = null;
        Throwable container = throwable;
        if (container == null) {
            container = npe = new NullPointerException("throwable");
        }
        for (int i = closeables.length -1; i >= 0; i--) {
            try {
                closeables[i].close();
            } catch (Exception e) {
                container.addSuppressed(e);
            }
        }
        if (npe != null) {
            throw npe;
        }
        return throwable;
    }
    
    public static <T extends Throwable> RuntimeException throwAs(Throwable t, Class<T> throwableClass) throws T {
        if (throwableClass.isInstance(t)) {
            throw (T) t;
        }
        return new RuntimeException(t);
    }
    
}
