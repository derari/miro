package org.cthul.miro.util;

public interface XRunnable<X extends Throwable> {

    void run() throws X;
    
}
