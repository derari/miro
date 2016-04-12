package org.cthul.miro.set.base;

import java.util.function.Consumer;
import org.cthul.miro.composer.Composer;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.graph.Graph;
import org.cthul.miro.map.impl.QueryableEntitySet;
import org.cthul.miro.composer.RequestComposer;
import org.cthul.miro.result.Results;
import org.cthul.miro.map.MappedStatement;
import org.cthul.miro.util.Key;

/**
 *
 * @param <Entity>
 * @param <Builder>
 * @param <This>
 */
public abstract class AbstractQuerySet<Entity, Builder, This extends AbstractQuerySet<Entity, Builder, This>> extends AbstractValueSet<Entity, This> {

    private final QueryableEntitySet<Entity> entitySet;
    private RequestComposer<? super MappedStatement<Entity, ? extends Builder>> composer;
//    private List<Entity> list = null;

    public AbstractQuerySet(QueryableEntitySet<Entity> entitySet) {
        this.entitySet = entitySet;
    }
    
    protected AbstractQuerySet(AbstractQuerySet<Entity, Builder, This> source) {
        super(source);
        this.entitySet = source.entitySet.copy();
        if (source.composer != null) {
            this.composer = source.composer.copy();
        }
    }
    
    protected abstract RequestComposer<? super MappedStatement<Entity, ? extends Builder>> newComposer();
    
    /*private*/ RequestComposer<? super MappedStatement<Entity, ? extends Builder>> getComposer() {
        if (composer == null) {
            composer = newComposer();
        }
        return composer;
    }
    
    /*private*/ QueryableEntitySet<Entity> getEntitySet() {
        return entitySet;
    }

    protected This withConnection(MiConnection cnn) {
        return doSafe(me -> me.getEntitySet().setConnection(cnn));
    }

    protected This withGraph(Graph graph) {
        return doSafe(me -> me.getEntitySet().setGraph(graph));
    }

    protected This withEntityType(EntityType<Entity> entityType) {
        return doSafe(me -> me.getEntitySet().setEntityType(entityType));
    }
    
    protected This compose(Consumer<? super Composer> action) {
        return doSafe(me -> action.accept(me.getComposer()));
    }
    
    protected <V> This setUp(Key<V> key, Consumer<V> action) {
        return compose(ic -> action.accept(ic.node(key)));
    }
    
    protected abstract Results.Action<Entity> result(QueryableEntitySet<Entity> entitySet, RequestComposer<? super MappedStatement<Entity, ? extends Builder>> composer);

    @Override
    public Results.Action<Entity> result() {
        return result(entitySet, getComposer());
    }
    
//    @Override
//    public List<Entity> asList() throws MiException {
//        if (list == null) {
//            Results.Action<Entity> result = result(entitySet, getComposer());
//            try {
//                list = result.asList();
//            } catch (InterruptedException ex) {
//                Thread.currentThread().interrupt();
//                throw new MiException("interrupted");
//            } catch (ExecutionException ex) {
//                throw new MiException(ex);
//            }
////            list = new AbstractList<Entity>() {
////                List<Entity> data = null;
////                Boolean empty = null;
////                List<Entity> result() {
////                    if (data == null) {
////                        data = result._asList();
////                    }
////                    return data;
////                }
////                @Override
////                public Iterator<Entity> iterator() {
////                    return result().iterator();
////                }
////                @Override
////                public Entity get(int index) {
////                    return result().get(index);
////                }
////                @Override
////                public boolean isEmpty() {
////                    if (data == null) {
////                        if (empty == null) {
////                            empty = composer._execute()._getFirst() == null;
////                        }
////                        return empty;
////                    }
////                    return result().isEmpty();
////                }
////                @Override
////                public int size() {
////                    return result().size();
////                }
////            };
//        }
//        return list;
//    }
}
