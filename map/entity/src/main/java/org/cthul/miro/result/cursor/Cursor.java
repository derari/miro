package org.cthul.miro.result.cursor;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class Cursor {

    protected Cursor() {
    }

    public static boolean hasNext(ResultCursor<?> cursor) {
        return cursor.hasNext();
    }

    public static boolean hasNext(CursorValue cursor) {
        return hasNext(resultCursor(cursor));
    }

    public static boolean hasNext(Object cursor) {
        return hasNext(resultCursor(cursor));
    }

    public static <V> V next(ResultCursor<V> cursor) {
        return cursor.next();
    }

    public static <V> V next(CursorValue cursor) {
        return next(resultCursor(cursor));
    }

    public static <V> V next(V cursor) {
        return next(resultCursor(cursor));
    }

    public static <V> V fixedCopy(ResultCursor<V> cursor) {
        return cursor.getFixCopy();
    }

    public static <V> V fixedCopy(CursorValue cursor) {
        return fixedCopy(resultCursor(cursor));
    }

    public static <V> V fixedCopy(V cursor) {
        return fixedCopy(resultCursor(cursor));
    }
    
    @SuppressWarnings("unchecked")
    public static <V> ResultCursor<V> resultCursor(V cursorValue) {
        if (cursorValue instanceof CursorValue) {
            return resultCursor((CursorValue) cursorValue);
        } else {
            return (ResultCursor<V>) CURSORS.get(cursorValue);
        }
    }

    @SuppressWarnings("unchecked")
    public static <V> ResultCursor<V> resultCursor(CursorValue cursorValue) {
        return (ResultCursor<V>) cursorValue.getResultCursor();
    }
    
    private static final Map<Object, ResultCursor<?>> CURSORS = 
                            Collections.synchronizedMap(new WeakHashMap<>());
    
    static void putResultCursor(Object cursorValue, ResultCursor<?> rc) {
        CURSORS.put(cursorValue, rc);
    }
}
