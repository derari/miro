package org.cthul.miro.result.cursor;

import java.util.*;

public abstract class ResultCursorBase<V> implements ResultCursor<V> {

    protected V cursor;
    protected long modCount = 0;

    public ResultCursorBase() {
    }

    public void setCursorValue(V cursor) {
        this.cursor = cursor;
        if (!(cursor instanceof CursorValue)) {
            Cursor.putResultCursor(cursor, this);
        }
    }

    @Override
    public V getValue() {
        return cursor;
    }

    protected abstract void makeNext();

    @Override
    public V next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        modCount++;
        makeNext();
        return cursor;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<V> iterator() {
        return new CursorIterator(modCount);
    }

    protected class CursorIterator implements Iterator<V> {

        private long lastMod;

        public CursorIterator(long lastMod) {
            this.lastMod = lastMod;
        }

        @Override
        public boolean hasNext() {
            return ResultCursorBase.this.hasNext();
        }

        @Override
        public V next() {
            if (lastMod != modCount) {
                throw new ConcurrentModificationException();
            }
            V value = ResultCursorBase.this.next();
            lastMod = modCount;
            return value;
        }

        @Override
        public void remove() {
            if (lastMod != modCount) {
                throw new ConcurrentModificationException();
            }
            ResultCursorBase.this.remove();
            lastMod = modCount;
        }
    }
}
