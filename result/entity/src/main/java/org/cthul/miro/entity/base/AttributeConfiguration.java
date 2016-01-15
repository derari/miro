package org.cthul.miro.entity.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityAttributes;
import org.cthul.miro.entity.EntityInitializer;

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
    
    private final List<MappingEntry<Entity>> entries = new ArrayList<>();

    public AttributeConfiguration() {
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
