package org.cthul.miro.cursor;

/**
 * Marker interface for values that are associated with a {@link ResultCursor}.
 * The value will change when {@link ResultCursor#next()} is called.
 */
public interface CursorValue {

    ResultCursor<?> getResultCursor();
}
