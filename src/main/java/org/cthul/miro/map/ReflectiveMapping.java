package org.cthul.miro.map;

import org.cthul.miro.map.z.*;
import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.cthul.miro.cursor.ResultCursor;
import java.lang.reflect.InvocationTargetException;

/**
 * A {@link SimpleMapping} that uses reflection to set fields and create entities by default.
 * @param <Type> 
 */
public class ReflectiveMapping<Type> extends AbstractMapping<Type> {

    private final Constructor<? extends Type> newRecord;
    private final Constructor<? extends Type> newCursor;

    public ReflectiveMapping(Class<Type> recordClass) {
        this(recordClass, null);
    }

    public ReflectiveMapping(Class<Type> recordClass, Class<? extends Type> cursorClass) {
        this(recordClass, recordClass, cursorClass);
    }
    
    public ReflectiveMapping(Class<Type> entityClass, Class<? extends Type> recordClass, Class<? extends Type> cursorClass) {
        super(entityClass);
        try {
            newRecord = recordClass.getConstructor();
            if (cursorClass != null) {
                newCursor = recordClass.getConstructor(ResultCursor.class);
            } else {
                newCursor = null;
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Type newRecord(Object[] args) {
        try {
            return newRecord.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Type newCursorValue(ResultCursor<? super Type> cursor) {
        try {
            if (newCursor != null) {
                return newCursor.newInstance(cursor);
            }
            return newRecord.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void setField(Type record, String field, ResultSet rs, int i) throws SQLException {
        injectField(record, field, rs, i);
    }

    @Override
    public void setField(Type record, String field, Object value) {
        injectField(record, field, value);
    }

    @Override
    public Object getField(Type entity, String field) {
        return peekField(entity, field);
    }
}
