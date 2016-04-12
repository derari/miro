package org.cthul.miro.set;

import org.cthul.miro.composer.Composer;

/**
 *
 */
public interface Impl {

    Composer impl();
    
    static Composer get(Object o) {
        return ((Impl) o).impl();
    }
}
