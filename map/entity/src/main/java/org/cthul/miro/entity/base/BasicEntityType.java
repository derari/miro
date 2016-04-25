package org.cthul.miro.entity.base;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import org.cthul.miro.entity.EntityFactory;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;

/**
 * A simple entity type that can create new entities without input
 * from the query result.
 * @param <Entity> entity type
 */
public abstract class BasicEntityType<Entity> implements EntityType<Entity> {
    
    public static <Entity> BasicEntityType<Entity> build(Supplier<Entity> factory, IntFunction<Entity[]> arrayFactory) {
        String name = null;
        try {
            name = factory.getClass().getMethod("get")
                    .getReturnType().getSimpleName();
        } catch (ReflectiveOperationException e) { /* not important */}
        return new BasicEntityType<Entity>(name) {
            @Override
            protected Entity newEntity() {
                return factory.get();
            }
            @Override
            public Entity[] newArray(int length) {
                return arrayFactory.apply(length);
            }
        };
    }
    
    public static <Entity> BasicEntityType<Entity> build(Class<? extends Entity> clazz) {
        String name = clazz.getSimpleName();
        Constructor<? extends Entity> newEntity; 
        try {
            newEntity = clazz.getConstructor();
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
        return new BasicEntityType<Entity>(name) {
            @Override
            protected Entity newEntity() {
                try {
                    return newEntity.newInstance();
                } catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
                    throw new RuntimeException(ex);
                }
            }
            @Override
            public Entity[] newArray(int length) {
                return (Entity[]) Array.newInstance(clazz, length);
            }
        };
    }

    private final String shortString;

    public BasicEntityType() {
        String name = null;
        try {
            name = getClass().getMethod("newEntity")
                    .getReturnType().getSimpleName();
        } catch (NoSuchMethodException | SecurityException e) { /* not important */}
        this.shortString = name;
    }

    public BasicEntityType(String shortString) {
        this.shortString = shortString;
    }
    
    @Override
    public EntityFactory<Entity> newFactory(MiResultSet rs) throws MiException {
        return new Factory(rs);
    }
    
    protected abstract Entity newEntity();

    @Override
    public String toString() {
        return getShortString();
    }

    protected String getShortString() {
        return shortString != null ? shortString : super.toString();
    }
    
    protected class Factory implements EntityFactory<Entity> {
        
//        protected final MiResultSet resultSet;

        public Factory(MiResultSet resultSet) {
//            this.resultSet = resultSet;
        }

        @Override
        public Entity newEntity() throws MiException {
            return BasicEntityType.this.newEntity();
        }

        @Override
        public void complete() throws MiException {
        }

        @Override
        public void close() throws MiException {
        }

        @Override
        public String toString() {
            return "new " + BasicEntityType.this.toString();
        }
    }
}
