package org.cthul.miro.composer;

import java.util.function.BiFunction;
import java.util.function.Function;
import org.cthul.miro.composer.template.Template;
import org.cthul.miro.composer.template.Templates;

/**
 *
 * @param <Key>
 * @param <Value>
 */
public interface MapNode<Key, Value> { //extends Templates.ComposableNode<MapNode<Key, Value>> 
    
    Value get(Key key);

//    @Override
//    public default Object and(MapNode<Key, Value> other) {
//        class Multi extends Templates.MultiNode<MapNode<Key, Value>>
//                    implements MapNode<Key, Value> {
//            public Multi() {
//                super(MapNode.this, other);
//            }
//            @Override
//            public void add(Key entry) {
//                all(MapNode::add, entry);
//            }
//        }
//        return new Multi();
//    }
    
    static <B,K,V> Template<B> template(Function<? super InternalComposer<? extends B>, ? extends Function<? super K,? extends V>> factory) {
        class MN implements MapNode<K,V>, Copyable<B> {
            final Function<? super K,? extends V> function;
            public MN(InternalComposer<? extends B> ic) {
                function = factory.apply(ic);
            }
            @Override
            public V get(K key) {
                return function.apply(key);
            }
            @Override
            public Object copyFor(InternalComposer<B> ic) {
                return new MN(ic);
            }
        }
        return Templates.newNode(ic -> new MN(ic));
    }
    
    static <B,K,V> Template<B> handle(BiFunction<? super InternalComposer<? extends B>, ? super K, ? extends V> action) {
        class MN implements MapNode<K,V>, Copyable<B> {
            final InternalComposer<? extends B> ic;
            public MN(InternalComposer<? extends B> ic) {
                this.ic = ic;
            }
            @Override
            public V get(K key) {
                return action.apply(ic, key);
            }
            @Override
            public Object copyFor(InternalComposer<B> ic) {
                return new MN(ic);
            }
        }
        return Templates.newNode(ic -> new MN(ic));
    }
}
