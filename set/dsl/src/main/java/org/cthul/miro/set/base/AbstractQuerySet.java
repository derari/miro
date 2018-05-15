package org.cthul.miro.set.base;

import java.util.function.Consumer;
import java.util.function.Function;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.graph.Graph;
import org.cthul.miro.db.stmt.MiQuery;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.map.MappedQueryComposer;
import org.cthul.miro.result.Results;
import org.cthul.miro.map.MappedQuery;
import org.cthul.miro.request.RequestComposer2;
import org.cthul.miro.request.part.BatchNode;

/**
 *
 * @param <Entity>
 * @param <Stmt>
 * @param <Cmp>
 * @param <This>
 */
public abstract class AbstractQuerySet<Entity, Stmt extends MiQuery, Cmp extends RequestComposer2<MappedQuery<Entity, Stmt>> & MappedQueryComposer, This extends AbstractQuerySet<Entity, Stmt, Cmp, This>> extends AbstractValueSet<Entity, This> {

    MiConnection cnn;
    RequestType<Stmt> requestType;
    private Cmp composer = null;

    public AbstractQuerySet(MiConnection cnn, RequestType<Stmt> requestType) {
        this.cnn = cnn;
        this.requestType = requestType;
    }

    protected AbstractQuerySet(AbstractQuerySet<Entity, Stmt, Cmp, This> source) {
        super(source);
        this.cnn = source.cnn;
        this.requestType = source.requestType;
        if (source.composer != null) {
            this.composer = (Cmp) source.composer.copy();
        }
    }
    
    protected abstract Cmp newComposer();
    
    /*private*/ Cmp getComposer() {
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
            me.getComposer().getType().setGraph(graph);
            if (graph instanceof MiConnection) me.cnn = (MiConnection) graph;
        });
    }

    protected This withEntityType(EntityType<Entity> entityType) {
        return doSafe(me -> me.getComposer().getType().setType(entityType));
    }
    
    protected This compose(Consumer<? super Cmp> action) {
        return doSafe(me -> action.accept(me.getComposer()));
    }
    
    protected <V> This setUp(Function<? super Cmp, ? extends V> key, Consumer<V> action) {
        return doSafe(me -> action.accept(key.apply(me.getComposer())));
    }

    protected <V> This setUp(Function<? super Cmp, ? extends BatchNode<V>> key, V... values) {
        return doSafe(me -> key.apply(me.getComposer()).set(values));
    }

    @Override
    protected Results.Action<Entity> buildResult() {
        MappedQuery<Entity, Stmt> qry = new MappedQuery<>(cnn, requestType);
        return qry.query(getComposer());
    }
}
