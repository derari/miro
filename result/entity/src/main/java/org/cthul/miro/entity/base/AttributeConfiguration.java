package org.cthul.miro.entity.base;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityAttributes;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.function.MiBiConsumer;

/**
 * A {@linkplain EntityConfiguration configuration} that maps columns 
 * from a result set to attributes.
 * @param <Entity>
 */
public class AttributeConfiguration<Entity> 
                implements AttributeMapping<Entity, RuntimeException, AttributeConfiguration<Entity>>,
                           EntityConfiguration<Entity>, EntityAttributes<Entity> {
    
    public static <Entity> AttributeConfiguration<Entity> build() {
        return new AttributeConfiguration<>();
    }
    
    public static <Entity> AttributeConfiguration<Entity> build(Class<Entity> entityClass) {
        return new AttributeConfiguration<>(entityClass);
    }
    
    private final List<MappingEntry<Entity>> entries = new ArrayList<>();
    
    private final Class<Entity> entityClass;

    public AttributeConfiguration() {
        this(null);
    }
    
    protected Setter<Entity> fieldSetter(String name) {
        if (entityClass == null) {
            throw new UnsupportedOperationException("Class required");
        }
        Field field = null;
        Class<?> clazz = entityClass;
        while (field == null && clazz != null) {
            Field[] dec = clazz.getDeclaredFields();
            for (Field f: dec) {
                if (f.getName().equals(name)) {
                    field = f;
                    break;
                }
            }
            clazz = clazz.getSuperclass();
        }
        if (field == null) {
            throw new IllegalArgumentException("No such field: " + name);
        }
        MiBiConsumer<Entity, Object> c = setter(field);
        return (e, rs, i) -> c.accept(e, rs.get(i));
    }
    
    private static <E> MiBiConsumer<E, Object> setter(Field f) {
        f.setAccessible(true);
        Class<?> c = f.getType();
        if (c == boolean.class) {
            return (e, o) -> {
                boolean b = o != null && (o.equals(true) ||
                        ((o instanceof Number) && ((Number) o).intValue() == 1));
                f.set(e, b);
            };
        }
        if (c == Boolean.class) {
            return (e, o) -> {
                if (o == null) {
                    f.set(e, null);
                } else {
                    boolean b = o.equals(true) ||
                            ((o instanceof Number) && ((Number) o).intValue() == 1);
                    f.set(e, b);
                }
            };
        }
        return f::set;
    }

    public AttributeConfiguration<Entity> field(String field) {
        return mapField(field, field);
    }
    
    public AttributeConfiguration<Entity> mapField(String column, String field) {
        return optional(column, fieldSetter(field));
    }

    public AttributeConfiguration<Entity> fields(String... field) {
        for (String f: field) {
            field(f);
        }
        return this;
    }
    
    public AttributeConfiguration<Entity> mapFields(String... mappings) {
        for (int i = 0; i < mappings.length; i++) {
            mapField(mappings[i], mappings[i+1]);
        }
        return this;
    }

    public AttributeConfiguration(Class<Entity> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public AttributeConfiguration<Entity> add(MappingEntry<Entity> entry) throws RuntimeException {
        entries.add(entry);
        return this;
    }
    
    public AttributeReader<Entity> newReader(MiResultSet resultSet) throws MiException {
        List<ReaderEntry<Entity>> readers = new ArrayList<>(entries.size());
        for (MappingEntry<Entity> me: entries) {
            ReaderEntry<Entity> re = me.newReader(resultSet);
            if (re != null) readers.add(re);
        }
        return new AttributeReader<>(resultSet, readers);
    }

    @Override
    public AttributeReader<Entity> newInitializer(MiResultSet rs) throws MiException {
        return newReader(rs);
    }

    @Override
    public EntityConfiguration<Entity> newConfiguration(List<?> attributes) {
        AttributeConfiguration copy = new AttributeConfiguration();
        for (Object a: attributes) {
            String s = Objects.toString(a);
            for (MappingEntry<Entity> e: entries) {
                if (e.setsAttribute(s)) {
                    copy.add(e);
                }
            }
        }
        return copy;
    }

    @Override
    public EntityInitializer<Entity> newInitializer(MiResultSet rs, List<?> attributes) throws MiException {
        return newConfiguration(attributes).newInitializer(rs);
    }

    @Override
    public String toString() {
        return entries.toString();
    }
}
