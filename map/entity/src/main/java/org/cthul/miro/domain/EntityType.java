package org.cthul.miro.domain;

import java.util.Collection;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.entity.*;
import org.cthul.miro.entity.builder.SelectorBuilder;
import org.cthul.miro.entity.map.ColumnMapping;

/**
 *
 * @param <Entity>
 */
public interface EntityType<Entity> {

    ColumnMapping mapToColumns(String prefix, Object key);
    
    void newEntityCreator(Repository repository, SelectorBuilder<Entity> selectorBuilder);

    EntityTemplate<Entity> newEntityLookUp(Repository repository, EntitySelector<Entity> selector);

    MappedTemplate<Entity> newEntityLookUp(Repository repository, MiConnection connection, EntitySelector<Entity> selector);
    
    EntityConfiguration<Entity> getPropertyReader(Repository repository, Collection<?> properties);

    void newPropertyLoader(Repository repository, MiConnection connection, Collection<?> properties, InitializationBuilder<? extends Entity> initBuilder);
    
    default EntitySelector<Entity> newEntityCreator(Repository repository) {
        return Entities.buildSelector(builder -> EntityType.this.newEntityCreator(repository, builder));
    }
    
    default EntityInitializer<Entity> newPropertyLoader(Repository repository, MiConnection connection, Collection<?> properties) {
        return Entities.buildInitializer(builder -> newPropertyLoader(repository, connection, properties, builder));
    }
//
//    void newPropertySetter(Repository repository, Map<String, Object> values, InitializationBuilder<Entity> initBuilder);
//    
//    default EntityInitializer<Entity> newPropertySetter(Repository repository, Map<String, Object> values) {
//        return Entities.buildInitializer(builder -> newPropertySetter(repository, values, builder));
//    }
}
