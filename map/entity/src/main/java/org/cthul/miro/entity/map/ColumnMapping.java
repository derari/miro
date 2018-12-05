package org.cthul.miro.entity.map;

import java.util.Collection;
import java.util.List;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.entity.FactoryBuilder;
import org.cthul.miro.domain.Repository;
import org.cthul.miro.entity.Entities;
import org.cthul.miro.entity.EntityFactory;

/**
 * Maps a set of values to database attributes.
 */
public interface ColumnMapping {
    
    /**
     * List of attributes that values are mapped to.
     * @return attribute names
     */
    List<String> getColumns();
    
    /**
     * Splits the {@code value} into atomic attributes and writes them into
     * {@code target} at {@code index}.
     * @param value
     * @param index
     * @param target
     * @return target
     */
    Object[] writeColumns(Object value, int index, Object[] target);
    
    /**
     * Return true if a {@linkplain #newValueReader(org.cthul.miro.db.MiResultSet, Cnn) value reader}
     * can be created for this result set.
     * @param resultSet
     * @return whether result set can be accepted
     * @throws MiException 
     */
    boolean accept(MiResultSet resultSet) throws MiException;

    /**
     * Configures the factory builder to create a factory that reads values
     * from the given result set.
     * @param repository
     * @param resultSet
     * @param factoryBuilder
     * @throws MiException 
     */
    void newValueReader(Repository repository, MiResultSet resultSet, FactoryBuilder<Object> factoryBuilder) throws MiException;
    
    default EntityFactory<Object> newValueReader(Repository repository, MiResultSet resultSet) throws MiException {
        return Entities.buildFactory(builder -> newValueReader(repository, resultSet, builder));
    }
    
    /**
     * Optional operation; if this attribute represents an entity type,
     * returns a mapping that maps the entity's properties.
     * @param attributes
     * @return nested attribute mapping
     */
    ColumnMapping nested(Collection<?> attributes);
}
