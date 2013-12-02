package org.cthul.miro.map;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.cthul.miro.cursor.ResultCursor;
import org.cthul.miro.result.*;
import org.cthul.miro.result.ResultBuilder;

/**
 * Maps the result of a query to instances of a class. Provides a
 * {@link EntityFactory} that creates instances, a {@link ValueAdapter} that
 * sets selected field values, and different {@link ResultBuilder}s.
 *
 * @param <Entity>
 */
public abstract class AbstractMapping<Entity> implements Mapping<Entity> {

    private final Class<Entity> entityClass;

    public AbstractMapping(Class<Entity> recordClass) {
        this.entityClass = recordClass;
    }

    protected Entity newRecord(Object[] args) {
        throw new UnsupportedOperationException("Records not supported");
    }

    protected Entity newCursorValue(ResultCursor<? super Entity> cursor) {
        throw new UnsupportedOperationException("Cursor not supported");
    }

    protected Entity copy(Entity e) {
        throw new UnsupportedOperationException("Copy not supported");
    }

    protected void setField(Entity record, String field, ResultSet rs, int i) throws SQLException {
        throw new IllegalArgumentException(
                "Cannot set field " + field + " of " + entityClass.getSimpleName());
    }

    protected void injectField(Entity record, String field, ResultSet rs, int i) throws SQLException {
        injectField(record, field, rs.getObject(i));
    }

    protected void injectField(Entity record, String field, Object value) {
        try {
            Field f = lookUpField(record, field);
            f.set(record, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected Object peekField(Entity record, String field) {
        try {
            Field f = lookUpField(record, field);
            return f.get(record);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    private Field lookUpField(Entity record, String field) throws NoSuchFieldException {
        Field f = null;
        Class<?> clazz = record.getClass();
        while (clazz != null && f == null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field df : fields) {
                if (df.getName().equals(field)) {
                    f = df;
                    break;
                }
            }
            clazz = clazz.getSuperclass();
        }
        if (f == null) {
            throw new NoSuchFieldException(field);
        }
        f.setAccessible(true);
        return f;
    }

    @Override
    public void setField(Entity record, String field, Object value) {
        throw new IllegalArgumentException(
                "Cannot set field " + field + " of " + entityClass.getSimpleName());
    }

    protected void setFields(Entity record, ResultSet rs, List<String> fields) throws SQLException {
        final int len = fields.size();
        for (int i = 0; i < len; i++) {
            setField(record, fields.get(i), rs, i + 1);
        }
    }

    @Override
    public EntityFactory<Entity> newFactory(ResultSet rs) throws SQLException {
        return new MappedEntityFactory(rs);
    }

    @Override
    public EntityConfiguration<Entity> newFieldConfiguration(List<String> fields) {
        return new FieldValuesConfiguration(fields);
    }
    
    @Override
    public Entity[] newArray(int length) {
        return (Entity[]) Array.newInstance(entityClass, length);
    }

    protected String[] getConstructorParameters() {
        return null;
    }

    protected class FieldValuesConfiguration extends AbstractEntityConfiguration<Entity> {        
        private final String[] fields;

        public FieldValuesConfiguration(Collection<String> fields) {
            this.fields = fields.toArray(new String[fields.size()]);
        }
        
        public FieldValuesConfiguration(String[] fields) {
            this.fields = fields;
        }

        @Override
        protected String[] getColumns() {
            return fields;
        }

        @Override
        protected void setColumn(Entity entity, int colIndex, ResultSet rs, int index) throws SQLException {
            setField(entity, fields[colIndex], rs, index);
        }
    }
    
    /**
     * Creates new instances, using constructor arguments from result set.
     * @see #getConstructorParameters()
     */
    protected class MappedEntityFactory extends EntityBuilderBase implements EntityFactory<Entity> {

        protected final ResultSet rs;
        private final int[] argColumns;
        private final Object[] argsBuf;

        public MappedEntityFactory(ResultSet rs) throws SQLException {
            this.rs = rs;
            String[] params = getConstructorParameters();
            if (params != null) {
                argColumns = getFieldIndices(rs, params);
                argsBuf = new Object[params.length];
            } else {
                argColumns = null;
                argsBuf = null;
            }
        }

        @Override
        public Entity newEntity() throws SQLException {
            if (argsBuf != null) {
                for (int i = 0; i < argsBuf.length; i++) {
                    argsBuf[i] = rs.getObject(argColumns[i]);
                }
            }
            return AbstractMapping.this.newRecord(argsBuf);
        }

        @Override
        public Entity newCursorValue(ResultCursor<? super Entity> rc) throws SQLException {
            return AbstractMapping.this.newCursorValue(rc);
        }

        @Override
        public Entity copy(Entity e) throws SQLException {
            return AbstractMapping.this.copy(e);
        }

        @Override
        public void close() throws SQLException {
        }
    };
}
