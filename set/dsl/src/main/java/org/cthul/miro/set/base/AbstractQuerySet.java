package org.cthul.miro.set.base;

import java.util.function.Consumer;
import org.cthul.miro.request.Composer;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.graph.Graph;
import org.cthul.miro.request.RequestComposer;
import org.cthul.miro.db.stmt.MiQuery;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.result.Results;
import org.cthul.miro.util.Key;
import org.cthul.miro.map.MappingKey;
import org.cthul.miro.map.layer.MappedQuery;
import org.cthul.miro.request.part.BatchNode;

/**
 *
 * @param <Entity>
 * @param <Stmt>
 * @param <This>
 */
public abstract class AbstractQuerySet<Entity, Stmt extends MiQuery, This extends AbstractQuerySet<Entity, Stmt, This>> extends AbstractValueSet<Entity, This> {

    MiConnection cnn;
    RequestType<Stmt> requestType;
    private RequestComposer<MappedQuery<Entity, Stmt>> composer = null;

    public AbstractQuerySet(MiConnection cnn, RequestType<Stmt> requestType) {
        this.cnn = cnn;
        this.requestType = requestType;
    }

    protected AbstractQuerySet(AbstractQuerySet<Entity, Stmt, This> source) {
        super(source);
        this.cnn = source.cnn;
        this.requestType = source.requestType;
        if (source.composer != null) {
            this.composer = source.composer.copy();
        }
    }
    
    protected abstract RequestComposer<MappedQuery<Entity, Stmt>> newComposer();
    
    /*private*/ RequestComposer<MappedQuery<Entity, Stmt>> getComposer() {
        if (composer == null) {
            composer = newComposer();
        }
        return composer;
    }
    
    protected This withConnection(MiConnection cnn) {
        return doSafe(me -> me.cnn = cnn);
    }

    protected This withGraph(Graph graph) {
        return doSafe(me -> {
            me.getComposer().node(MappingKey.TYPE).setGraph(graph);
            if (graph instanceof MiConnection) me.cnn = (MiConnection) graph;
        });
    }

    protected This withEntityType(EntityType<Entity> entityType) {
        return doSafe(me -> me.getComposer().node(MappingKey.TYPE).setType(entityType));
    }
    
    protected This compose(Consumer<? super Composer> action) {
        return doSafe(me -> action.accept(me.getComposer()));
    }
    
    protected <V> This setUp(Key<V> key, Consumer<V> action) {
        return compose(ic -> action.accept(ic.node(key)));
    }

    protected <V> This setUp(Key<? extends BatchNode<V>> key, V... values) {
        return compose(ic -> ic.node(key).set(values));
    }

    @Override
    public Results.Action<Entity> result() {
        MappedQuery<Entity, Stmt> qry = new MappedQuery<>(cnn, requestType);
        return qry.query(getComposer());
    }
}
