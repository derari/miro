package org.cthul.miro.map;

import org.cthul.miro.composer.StatementHolder;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.entity.EntityTypes;

/**
 *
 * @param <Entity>
 * @param <Statement>
 */
public interface MappedBuilder<Entity, Statement>
                 extends StatementHolder<Statement> {

    void configureWith(EntityConfiguration<? super Entity> config);
    
    default void initializeWith(EntityInitializer<? super Entity> init) {
        configureWith(EntityTypes.asConfiguration(init));
    }
    
    @Override
    Statement getStatement();
}
