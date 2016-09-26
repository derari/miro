package org.cthul.miro.util;

import java.util.function.Consumer;

/**
 *
 */
public class LazyDescription implements Description {

    private final Consumer<? super Description> init;
    private StringBuilder sb = null;
    private String string;

    public LazyDescription(Consumer<? super Description> init) {
        this.init = init;
    }

    @Override
    public void append(String text) {
        if (sb == null) throw new IllegalStateException();
        sb.append(text);
    }

    @Override
    public String toString() {
        if (string == null) {
            string = "building...";
            sb = new StringBuilder();
            init.accept(this);
            string = sb.toString();
            sb = null;
        }
        return string;
    }
}
