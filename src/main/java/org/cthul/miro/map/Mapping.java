package org.cthul.miro.map;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.cthul.miro.cursor.ResultCursor;
import org.cthul.miro.map.ResultBuilder.EntityFactory;
import org.cthul.miro.map.ResultBuilder.ValueAdapter;

/**
 * Maps the result of a query to instances of a class. Provides a
 * {@link EntityFactory} that creates instances, a {@link ValueAdapter} that
 * sets selected field values, and different {@link ResultBuilder}s.
 *
 * @param <Entity>
 */
public abstract class Mapping<Entity> {

    private final Class<Entity> entityClass;
    private ResultBuilder<Entity[], Entity> arrayResultBuilder = null;

    public Mapping(Class<Entity> recordClass) {
        this.entityClass = recordClass;
    }

    protected Entity newRecord(Object[] args) {
        throw new UnsupportedOperationException("Records not supported");
    }

    protected Entity newCursorValue(ResultCursor<Entity> cursor) {
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

    protected void injectField(Entity record, String field, Object value) throws SQLException {
        try {
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
            f.set(record, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void setField(Entity record, String field, Object value) throws SQLException {
        throw new IllegalArgumentException(
                "Cannot set field " + field + " of " + entityClass.getSimpleName());
    }

    protected void setFields(Entity record, ResultSet rs, List<String> fields) throws SQLException {
        final int len = fields.size();
        for (int i = 0; i < len; i++) {
            setField(record, fields.get(i), rs, i + 1);
        }
    }

    public ValueAdapter<Entity> newValueAdapter(String... fields) {
        return new FieldValueAdapter(fields);
    }

    public ValueAdapter<Entity> newValueAdapter(List<String> fields) {
        return new FieldValueAdapter(fields);
    }

    public EntityFactory<Entity> newEntityFactory() {
        return new MappedEntityFactory();
    }
    
    public Entity[] newArray(int length) {
        return (Entity[]) Array.newInstance(entityClass, length);
    }

    public ResultBuilder<List<Entity>, Entity> asList() {
        return ResultBuilders.getListResult();
    }

    public ResultBuilder<Entity[], Entity> asArray() {
        if (arrayResultBuilder == null) {
            arrayResultBuilder = ResultBuilders.getArrayResult(entityClass);
        }
        return arrayResultBuilder;
    }
    
    public ResultBuilder<Entity[], Entity> asArray(ResultBuilder<? extends Collection<? extends Entity>, Entity> listResult) {
        return ResultBuilders.getArrayResult(entityClass, listResult);
    }

    public ResultBuilder<ResultCursor<Entity>, Entity> asCursor() {
        return ResultBuilders.getCursorResult();
    }

    public ResultBuilder<Entity, Entity> getSingle() {
        return ResultBuilders.getSingleResult();
    }

    public ResultBuilder<Entity, Entity> getFirst() {
        return ResultBuilders.getFirstResult();
    }

    protected String[] getConstructorParameters() {
        return null;
    }

    /**
     * Sets fields from result set.
     */
    protected class FieldValueAdapter extends ValueAdapterBase<Entity> {

        private final String[] fields;
        private ResultSet rs = null;
        private int[] fieldIndices = null;

        public FieldValueAdapter(String[] fields) {
            this.fields = fields;
        }
        
        public FieldValueAdapter(List<String> fields) {
            this(fields.toArray(new String[fields.size()]));
        }

        @Override
        public void initialize(ResultSet rs) throws SQLException {
            this.rs = rs;
            this.fieldIndices = getFieldIndices(rs, fields);
        }

        @Override
        public void apply(Entity entity) throws SQLException {
            int len = fieldIndices.length;
            for (int i = 0; i < len; i++) {
                setField(entity, fields[i], rs, fieldIndices[i]);
            }
        }

        @Override
        public void complete() throws SQLException {
        }

        @Override
        public void close() throws SQLException {
        }
    };

    /**
     * Creates new instances, using constructor arguments from result set.
     * @see #getConstructorParameters()
     */
    protected class MappedEntityFactory implements EntityFactory<Entity> {

        protected ResultSet rs = null;
        private int[] argColumns = null;
        private Object[] argsBuf = null;

        @Override
        public void initialize(ResultSet rs) throws SQLException {
            this.rs = rs;
            String[] params = getConstructorParameters();
            if (params != null) {
                argColumns = new int[params.length];
                for (int i = 0; i < params.length; i++) {
                    argColumns[i] = rs.findColumn(params[i]);
                }
                argsBuf = new Object[params.length];
            }
        }

        @Override
        public Entity newEntity() throws SQLException {
            if (argsBuf != null) {
                for (int i = 0; i < argsBuf.length; i++) {
                    argsBuf[i] = rs.getObject(argColumns[i]);
                }
            }
            return Mapping.this.newRecord(argsBuf);
        }

        @Override
        public Entity newCursorValue(ResultCursor<Entity> rc) throws SQLException {
            return Mapping.this.newCursorValue(rc);
        }

        @Override
        public Entity copy(Entity e) throws SQLException {
            return Mapping.this.copy(e);
        }

        @Override
        public void close() throws SQLException {
        }
    };
}
