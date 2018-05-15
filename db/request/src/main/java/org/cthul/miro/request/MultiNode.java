package org.cthul.miro.request;

import java.util.List;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 *
 */
public class MultiNode<C,N> implements Initializable<C>, Copyable2<C> {
        
    private final Function<? super C, ? extends List<? extends N>> init;
    private List<? extends N> nodes = null;

    public MultiNode(Function<? super C, ? extends List<? extends N>> init) {
        this.init = init;
    }

    @Override
    public void initialize(C composer) {
        nodes = init.apply(composer);
    }
    
    protected MultiNode<C,N> newInstance(Function<? super C, ? extends List<? extends N>> init) {
        try {
            return getClass().getConstructor(Function.class).newInstance(init);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object copy(C composer) {
        MultiNode<C,N> copy = newInstance(init);
        copy.initialize(composer);
        return copy;
    }
        
    protected void all(Consumer<N> action) {
        nodes.forEach(action);
    }

    protected <T> void all(BiConsumer<N, T> action, T arg) {
        all(n -> action.accept(n, arg));
    }

    @Override
    public String toString() {
        if (nodes == null) return "MultiNode";
        return nodes.stream().map(Object::toString).collect(Collectors.joining(","));
    }
}