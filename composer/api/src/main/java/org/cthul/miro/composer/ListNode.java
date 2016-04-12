package org.cthul.miro.composer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import org.cthul.miro.composer.template.Template;
import org.cthul.miro.composer.template.Templates;

/**
 *
 * @param <Entry>
 */
public interface ListNode<Entry> extends Templates.ComposableNode<ListNode<Entry>> {
    
    void add(Entry entry);
    
    default void addAll(Entry... entries) {
        for (Entry e: entries) add(e);
    }
    
    default void addAll(Iterable<? extends Entry> entries) {
        entries.forEach(this::add);
    }
    
    default void addAll(Stream<? extends Entry> entries) {
        entries.forEach(this::add);
    }

    @Override
    public default Object and(ListNode<Entry> other) {
        class Multi extends Templates.MultiNode<ListNode<Entry>>
                    implements ListNode<Entry> {
            public Multi() {
                super(ListNode.this, other);
            }
            @Override
            public void add(Entry entry) {
                all(ListNode::add, entry);
            }
        }
        return new Multi();
    }
    
    static <B,E> Template<B> template(Function<? super InternalComposer<? extends B>, ? extends Consumer<? super E>> factory) {
        class LN implements ListNode<E>, Copyable<B> {
            final Consumer<? super E> consumer;
            public LN(InternalComposer<? extends B> ic) {
                consumer = factory.apply(ic);
            }
            @Override
            public void add(E entry) {
                consumer.accept(entry);
            }
            @Override
            public Object copyFor(InternalComposer<B> ic) {
                return new LN(ic);
            }
        }
        return Templates.newNode(ic -> new LN(ic));
    }
    
    static <B,E> Template<B> handle(BiConsumer<? super InternalComposer<? extends B>, ? super E> action) {
        class LN implements ListNode<E>, Copyable<B> {
            final InternalComposer<? extends B> ic;
            public LN(InternalComposer<? extends B> ic) {
                this.ic = ic;
            }
            @Override
            public void add(E entry) {
                action.accept(ic, entry);
            }
            @Override
            public Object copyFor(InternalComposer<B> ic) {
                return new LN(ic);
            }
        }
        return Templates.newNode(ic -> new LN(ic));
    }
}
