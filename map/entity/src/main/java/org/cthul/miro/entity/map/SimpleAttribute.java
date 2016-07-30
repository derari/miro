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
 * @param <Cnn>
 */
public abstract class SimpleAttribute<Entity, Cnn> implements EntityAttribute<Entity, Cnn> {
    
    private final String key;
    private final ColumnMapping columnValue;

    public SimpleAttribute(String key, ColumnMapping columnValue) {
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
    public EntityFactory<?> newValueReader(MiResultSet rs, Cnn cnn) throws MiException {
        return columnValue.newValueReader(rs, cnn);
    }

    public static class ReadWrite<Entity, Cnn> extends SimpleAttribute<Entity, Cnn> {
        private final Function<? super Entity, Object> getter;
        private final XBiConsumer<? super Entity, Object, MiException> setter;

        public ReadWrite(String key, ColumnMapping columnValue, Function<? super Entity, Object> getter, XBiConsumer<? super Entity, Object, MiException> setter) {
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
    
    public static class Builder<Entity, Cnn> extends SimpleAttributeBuilder<Entity, Cnn, EntityAttribute<Entity, Cnn>> {

        public Builder() {
        }

        public Builder(Class<Entity> clazz, String key) {
            super(clazz, key);
        }

        @Override
        protected EntityAttribute<Entity, Cnn> build(EntityAttribute<Entity, Cnn> field) {
            return field;
        }
    }
}
