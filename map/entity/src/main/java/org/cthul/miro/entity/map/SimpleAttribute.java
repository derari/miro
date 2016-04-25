package org.cthul.miro.entity.map;

import java.util.List;
import java.util.function.Function;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.entity.EntityFactory;
import org.cthul.miro.util.XBiConsumer;

/**
 *
 * @param <Entity>
 */
public abstract class SimpleAttribute<Entity> implements EntityAttribute<Entity> {
    
    private final String key;
    private final ColumnValue columnValue;

    public SimpleAttribute(String key, ColumnValue columnValue) {
        this.key = key;
        this.columnValue = columnValue;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public List<String> getColumns() {
        return columnValue.getColumns();
    }

    @Override
    public Object[] toColumns(Object value, Object[] result) {
        return columnValue.toColumns(value, result);
    }

    @Override
    public EntityFactory<?> newValueReader(MiResultSet rs) throws MiException {
        return columnValue.newValueReader(rs);
    }

    public static class ReadWrite<Entity> extends SimpleAttribute<Entity> {
        private final Function<? super Entity, Object> getter;
        private final XBiConsumer<? super Entity, Object, MiException> setter;

        public ReadWrite(String key, ColumnValue columnValue, Function<? super Entity, Object> getter, XBiConsumer<? super Entity, Object, MiException> setter) {
            super(key, columnValue);
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public Object get(Entity e) {
            return getter.apply(e);
        }

        @Override
        public void set(Entity e, Object value) throws MiException {
            setter.accept(e, value);
        }
    }
    
    public static class Builder<Entity> extends SimpleAttributeBuilder<Entity, EntityAttribute<Entity>> {

        public Builder() {
        }

        public Builder(Class<Entity> clazz, String key) {
            super(clazz, key);
        }

        @Override
        protected EntityAttribute<Entity> build(EntityAttribute<Entity> field) {
            return field;
        }
    }
}
