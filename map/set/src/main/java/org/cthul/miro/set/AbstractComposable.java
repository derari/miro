package org.cthul.miro.set;

import java.util.function.Consumer;
import java.util.function.Function;
import org.cthul.miro.composer.RequestComposer;
import org.cthul.miro.composer.node.BatchNode;

/**
 *
 * @param <Cmp>
 * @param <This>
 */
public abstract class AbstractComposable<Cmp, This extends AbstractComposable<Cmp, This>> extends AbstractImmutable<This> {

    private Cmp composer = null;
    
    public AbstractComposable() {
    }

    public AbstractComposable(AbstractComposable<Cmp, This> source) {
        super(source);
        composer = RequestComposer.copyRequest(source.composer);
    }

    @Override
    protected void initialize() {
        super.initialize();
        composer = newComposer();
    }

    protected abstract Cmp newComposer();

    protected Cmp getComposer() {
        return composer;
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
}
