package org.cthul.miro.entity.map;

import java.util.Collection;
import java.util.function.Function;
import org.cthul.miro.db.MiException;
import org.cthul.miro.util.XBiConsumer;

/**
 *
 * @param <Entity>
 */
public abstract class SimpleProperty<Entity> implements MappedProperty<Entity> {
    
    private final String key;
    protected final ColumnMapping attributeMapping;

    public SimpleProperty(String key, ColumnMapping attributeMapping) {
        this.key = key;
        this.attributeMapping = attributeMapping;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public ColumnMapping getMapping() {
        return attributeMapping;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "/" + getKey();
    }

    @Override
    public MappedProperty<Entity> nested(Collection<?> properties) {
        return nested(properties, attributeMapping);
    }

    protected MappedProperty<Entity> nested(Collection<?> properties, ColumnMapping columns) {
        return new SimpleProperty<Entity>(key, columns.nested(properties)) {
            @Override
            public Object get(Entity e) {
                return SimpleProperty.this.get(e);
            }
            @Override
            public void set(Entity e, Object value) throws MiException {
                SimpleProperty.this.set(e, value);
            }
            @Override
            public MappedProperty<Entity> nested(Collection<?> properties) {
                return SimpleProperty.this.nested(properties, this.attributeMapping);
            }
        };
    }

    public static class ReadWrite<Entity> extends SimpleProperty<Entity> {
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
    
//    public static class Builder<Entity, Cnn> extends SimplePropertyBuilder<Entity, Cnn, MappedProperty<Entity, Cnn>> {
//
//        public Builder() {
//        }
//
//        public Builder(Class<Entity> clazz, String key) {
//            super(clazz, key);
//        }
//
//        @Override
//        protected MappedProperty<Entity, Cnn> build(MappedProperty<Entity, Cnn> field) {
//            return field;
//        }
//    }
}
