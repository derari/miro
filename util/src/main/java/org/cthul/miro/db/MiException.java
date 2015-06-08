package org.cthul.miro.db;

/**
 *
 */
public class MiException extends Exception {
    
    private static final long serialVersionUID = 1L;

    public MiException() {
    }

    public MiException(String msg) {
        super(msg);
    }

    public MiException(String message, Throwable cause) {
        super(message, cause);
    }

    public MiException(Throwable cause) {
        super(cause);
    }
}
