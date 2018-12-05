package org.cthul.miro.util;

import java.util.AbstractList;

/**
 *
 */
public class ReadOnlyArrayList<T> extends AbstractList<T> {
    
    private final Object[] data;

    public ReadOnlyArrayList(T... data) {
        this.data = data;
    }

    @Override
    public T get(int index) {
        if (data == null) throw new IndexOutOfBoundsException(""+ index);
        return (T) data[index];
    }

    @Override
    public int size() {
        return data == null ? 0 : data.length;
    }
}
