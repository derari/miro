package org.cthul.miro.entity.base;

import java.util.Objects;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.function.MiConsumer;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.InitializationBuilder;
import org.cthul.miro.util.Closeables;

/**
 * A stateless entity initializer that does not require input from the
 * query result.
 * @param <Entity> entity type
 */
public abstract class EntityPostProcessing<Entity> implements EntityConfiguration<Entity>, EntityInitializer<Entity> {
    
    private final String shortString;

    public EntityPostProcessing() {
        this.shortString = null;
    }

    public EntityPostProcessing(Object shortString) {
        this.shortString = Objects.toString(shortString);
    }
    
    @Override
    public EntityInitializer<Entity> newInitializer(MiResultSet rs) {
        return asInitializer();
    }

    @Override
    public void newInitializer(MiResultSet resultSet, InitializationBuilder<? extends Entity> builder) throws MiException {
        builder.add(newInitializer(resultSet));
    }
    
    public EntityInitializer<Entity> asInitializer() {
        return this;
    }
    
    public EntityConfiguration<Entity> asConfiguration() {
        return this;
    }
    
    @Override
    public abstract void apply(Entity entity) throws MiException;

    @Override
    public void complete() throws MiException {
    }

    @Override
    public String toString() {
        return "-> " + getShortString();
    }

    protected String getShortString() {
        return shortString != null ? shortString : super.toString();
    }
    
    public static <Entity> EntityPostProcessing<Entity> apply(MiConsumer<Entity> processingStep) {
        return new EntityPostProcessing<Entity>() {
            @Override
            public void apply(Entity entity) throws MiException {
                try {
                    processingStep.accept(entity);
                } catch (Exception e) {
                    throw Closeables.exceptionAs(e, MiException.class);
                }
            }
            @Override
            protected String getShortString() {
                return processingStep.toString();
            }
        };
    }
}
