package org.cthul.miro.set.base;

import java.util.function.Consumer;
import java.util.function.Function;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.request.MiQuery;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.map.MappedQueryComposer;
import org.cthul.miro.result.Results;
import org.cthul.miro.map.MappedQuery;
import org.cthul.miro.composer.node.BatchNode;
import org.cthul.miro.composer.RequestComposer;
import org.cthul.miro.domain.Repository;
import org.cthul.miro.entity.EntityTemplate;

/**
 *
 * @param <Entity>
 * @param <Req>
 * @param <Cmp>
 * @param <This>
 */
public abstract class AbstractQuerySet<Entity, Req extends MiQuery, Cmp extends RequestComposer<MappedQuery<Entity, Req>> & MappedQueryComposer, This extends AbstractQuerySet<Entity, Req, Cmp, This>> extends AbstractValueSet<Entity, This> {

    MiConnection cnn;
    private final RequestType<Req> requestType;
    private Cmp composer = null;

    public AbstractQuerySet(MiConnection cnn, RequestType<Req> requestType) {
        this.cnn = cnn;
        this.requestType = requestType;
    }

    protected AbstractQuerySet(AbstractQuerySet<Entity, Req, Cmp, This> source) {
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

    protected This withRepository(Repository repository) {
        return doSafe(me -> {
            me.getComposer().getType().setRepository(repository);
            if (repository instanceof MiConnection) {
                me.cnn = (MiConnection) repository;
            }
        });
    }

    protected This withTemplate(EntityTemplate<Entity> template) {
        return doSafe(me -> me.getComposer().getType().setTemplate(template));
    }
    
    protected This compose(Consumer<? super Cmp> action) {
        return doSafe(me -> action.accept(me.getComposer()));
    }
    
    protected <V> This setUp(Function<? super Cmp, ? extends V> key, Consumer<V> action) {
        return doSafe(me -> action.accept(key.apply(me.getComposer())));
    }

    protected <V> This setUp(Function<? super Cmp, ? extends BatchNode<V>> key, V... values) {
        return doSafe(me -> key.apply(me.getComposer()).batch(values));
    }

    @Override
    protected Results.Action<Entity> buildResult() {
        MappedQuery<Entity, Req> qry = new MappedQuery<>(cnn, requestType);
        return qry.apply(getComposer()).result();
    }
}
