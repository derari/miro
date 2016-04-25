package org.cthul.miro.set;

import org.cthul.miro.request.Composer;

/**
 *
 */
public interface Impl {

    Composer impl();
    
    static Composer get(Object o) {
        return ((Impl) o).impl();
    }
}
