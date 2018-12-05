package org.cthul.miro.entity.map;

import org.cthul.miro.util.XBiConsumer;
import java.lang.reflect.Field;
import java.util.function.Function;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.map.ResultColumns.ColumnMatcher;
import org.cthul.miro.entity.map.ResultColumns.ColumnRule;
import org.cthul.miro.entity.map.ResultColumns.ColumnsMatcher;

/**
 *
 * @param <Entity>
 * @param <Result>
 */
public interface MappedPropertyBuilder<Entity, Result> {
    
    ColumnMappingBuilder<Entity, Single<Entity, Result>, Group<Entity, Result>> property(String key);
    
    interface FieldAccess<Entity, Result, This extends FieldAccess<Entity, Result, This>> {
        
        Class<Entity> entityClass();
        
        This get(Function<Entity, Object> getter);
        
        Result set(XBiConsumer<? super Entity, ?, MiException> setter);
        
        default Result readOnly() {
            return set(READ_ONLY);
        }
        
        default This get(Field f) {
            f.setAccessible(true);
            class FieldGetter implements Function<Entity, Object> {
                @Override
                public Object apply(Entity e) {
                    try {
                        return f.get(e);
                    } catch (ReflectiveOperationException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            return get(new FieldGetter());
        }
        
        default This get(String field) {
            return get(Single.find(entityClass(), field));
        }
        
        default Result readOnly(Function<Entity, Object> getter) {
            return get(getter).readOnly();
        }
        
        default Result readOnly(Field f) {
            return get(f).readOnly();
        }
        
        default Result readOnly(String field) {
            return get(field).readOnly();
        }
        
        default Result set(Field field) {
            field.setAccessible(true);
            class FieldSetter implements XBiConsumer<Object, Object, MiException> {
                @Override
                public void accept(Object t, Object u) throws MiException {
                    try {
                        if (field.getType() == int.class) {
                            // TODO: rework all this setting to Field
                            u = ((Number) u).intValue();
                        }
                        field.set(t, u);
                    } catch (ReflectiveOperationException ex) {
                        throw new MiException(ex);
                    }
                }
            }
            return set(new FieldSetter());
        }
        
        default Result set(String field) {
            return set(Single.find(entityClass(), field));
        }
        
        default Result field(Field field) {
            return get(field).set(field);
        }
        
        default Result field(String field) {
            return field(Single.find(entityClass(), field));
        }
    }
    
    abstract class Single<Entity, Result> 
                    extends ColumnMappingBuilder.Single<Entity, Single<Entity, Result>>
                    implements FieldAccess<Entity, Result, Single<Entity, Result>> {
        
        private final String key;
        private final Class<Entity> clazz;
        private Function<? super Entity, Object> getter = WRITE_ONLY;

        public Single(String key, Class<Entity> clazz, ColumnMatcher column) {
            super(column);
            this.key = key;
            this.clazz = clazz;
        }

        protected abstract Result build(MappedProperty<Entity> f);

        @Override
        public Class<Entity> entityClass() {
            return clazz;
        }

        @Override
        public Single<Entity, Result> get(Function<Entity, Object> getter) {
            this.getter = getter;
            return this;
        }
        
        @Override
        public Result set(XBiConsumer<? super Entity, ?, MiException> setter) {
            ColumnMapping cv = buildColumnValue();
            MappedProperty<Entity> f;
            f = new SimpleProperty.ReadWrite<>(key, cv, getter, (XBiConsumer) setter);
            return build(f);
        }

        @Override
        public Result set(Field field) {
            if (toValue == null) {
                Class<?> c = field.getType();
                if (c == boolean.class) {
                    toValue = ColumnMappingBuilder.TO_BOOL;
                } else if (c == Boolean.class) {
                    toValue = ColumnMappingBuilder.TO_BOOLEAN;
                }
            }
            return FieldAccess.super.set(field);
        }
        
        protected static Field find(Class<?> clazz, String field) {
            if (clazz == null) throw new UnsupportedOperationException("Class required");
            Class<?> c = clazz;
            Field f = null;
            while (c != null) {
                try {
                    f = c.getDeclaredField(field);
                } catch (ReflectiveOperationException e) { }
                c = c.getSuperclass();
            }
            if (f == null) {
                throw new IllegalArgumentException(field);
            }
            return f;
        }
    }
    
    abstract class Group<Entity, Result> 
                    extends ColumnMappingBuilder.Group<Entity, Group<Entity, Result>>
                    implements FieldAccess<Entity, Result, Group<Entity, Result>> {
        
        private final String key;
        private final Class<Entity> clazz;
        private Function<? super Entity, Object> getter = WRITE_ONLY;

        public Group(String key, Class<Entity> clazz, String[] columns, ColumnRule allRule, ColumnRule eachRule) {
            super(columns, allRule, eachRule);
            this.key = key;
            this.clazz = clazz;
        }

        public Group(String key, Class<Entity> clazz, String[] columns, ColumnsMatcher matcher) {
            super(columns, matcher);
            this.key = key;
            this.clazz = clazz;
        }
        
        protected abstract Result build(MappedProperty<Entity> f);

        @Override
        public Class<Entity> entityClass() {
            return clazz;
        }
        
        @Override
        public Group<Entity, Result> get(Function<Entity, Object> getter) {
            this.getter = getter;
            return this;
        }

        @Override
        public Result set(XBiConsumer<? super Entity, ?, MiException> setter) {
            ColumnMapping cv = buildColumnValue();
            MappedProperty<Entity> f = new SimpleProperty.ReadWrite<>(key, cv, getter, (XBiConsumer) setter);
            return build(f);
        }
    }

    static final Function<Object, Object> WRITE_ONLY = e -> {throw new UnsupportedOperationException("Write only"); };    
    static final XBiConsumer<Object, Object, MiException> READ_ONLY = (e, o) -> { throw new MiException("Read only"); };
}
