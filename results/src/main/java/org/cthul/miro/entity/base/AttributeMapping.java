package org.cthul.miro.entity.base;

import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;

/**
 * A {@linkplain EntityConfiguration configuration} that maps columns 
 * from a result set to attributes.
 * @param <Entity>
 */
public class AttributeMapping<Entity> extends AttributeMappingBase<Entity, RuntimeException, AttributeMapping<Entity>> implements EntityConfiguration<Entity> {
    
    public static <Entity> AttributeMapping<Entity> build() {
        return new AttributeMapping<>();
    }
    
    private final List<MappingEntry<Entity>> entries = new ArrayList<>();

    public AttributeMapping() {
    }

    @Override
    protected AttributeMapping<Entity> add(MappingEntry<Entity> entry) throws RuntimeException {
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
    public String toString() {
        return entries.toString();
    }
    
    public static interface Setter<Entity> {
        void set(Entity e, MiResultSet rs, int index) throws MiException;
    }
    
    public static interface GroupSetter<Entity> {
        void set(Entity e, MiResultSet rs, int[] indices) throws MiException;
    }

}
