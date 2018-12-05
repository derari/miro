package org.cthul.miro.map;

import java.util.Collection;
import java.util.List;
import org.cthul.miro.map.node.MappedQueryNodeFactory;
import org.cthul.miro.composer.ComposerState;
import org.cthul.miro.composer.RequestComposer;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.request.MiQuery;
import org.cthul.miro.domain.Repository;
import org.cthul.miro.domain.impl.AbstractTypeBuilder;
import org.cthul.miro.entity.EntityTemplate;

/**
 *
 * @param <Entity>
 * @param <This>
 */
public abstract class AbstractQueryableType<Entity, This extends AbstractQueryableType<Entity, This>> extends AbstractTypeBuilder<Entity, This> {
    
    private MappedQueryComposer<Entity> batchComposer = null;

    public AbstractQueryableType(Class<Entity> clazz) {
        super(clazz);
    }

    public AbstractQueryableType(Class<Entity> clazz, Object shortString) {
        super(clazz, shortString);
    }
    
    protected MappedQueryComposer<Entity> newMappedQueryComposer() {
        return new MappedQueryNodeFactory(this, entityClass(), () -> newEntityTemplate(null)).newComposer();
    }
    
    protected abstract MappedQueryComposer<Entity> newBatchComposer();
    
    protected abstract MiQuery newBatchQuery(MiConnection cnn);
    
    protected MappedQueryComposer<Entity> newBatchComposer(Repository repository, MiConnection cnn, Collection<?> attributes) {
        if (batchComposer == null) {
            batchComposer = newBatchComposer();
        }
        MappedQueryComposer<Entity> batch = ComposerState.copy(batchComposer);
        batch.getType().setRepository(repository);
        batch.getIncludedProperties().addAll(getKeys());
        batch.getFetchedProperties().addAll(flattenStr(attributes));
        return batch;
    }

    @Override
    protected BatchLoader<Entity> newBatchLoader(Repository repository, MiConnection connection, Collection<?> properties) {
        String[] keyArray = getKeys().toArray(new String[0]);
        return new AbstractBatchLoader(repository, properties) {
            MappedQueryComposer<Entity> batch = null;

            @Override
            protected void fillProperties(EntityTemplate<Entity> template, List<Object[]> keys) throws MiException {
                if (batch == null) {
                    batch = AbstractQueryableType.this.newBatchComposer(repository, connection, properties);
                    batch.getType().setTemplate(template);
                }
                MappedQueryComposer<Entity> cmp = ComposerState.copy(batch);
                cmp.getPropertyFilter().forProperties(keyArray).addAll(keys);
                MappedQuery<Entity, MiQuery> query = new MappedQuery<>(newBatchQuery(connection));
                query.apply((RequestComposer<MappedQuery<Entity,MiQuery>>) cmp)
                        .result()._get().noResult();
            }
        };
    }

    @Override
    protected EntityTemplate<Entity> newEntityTemplate(Repository repository) {
        return super.newEntityTemplate(repository);
    }
}
