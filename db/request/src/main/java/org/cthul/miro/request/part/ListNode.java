package org.cthul.miro.request.part;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.cthul.miro.request.MultiNode;
import org.cthul.miro.request.template.Template;
import org.cthul.miro.request.template.Templates;
import org.cthul.miro.request.template.InternalComposer;

/**
 *
 * @param <Entry>
 */
public interface ListNode<Entry> extends Templates.ComposableNode<ListNode<Entry>>, BatchNode<Entry> {
    
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
    public default void batch(Collection<? extends Entry> values) {
        addAll(values);
    }

    @Override
    public default void batch(Entry... values) {
        addAll(values);
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
    
    static <C,T> ListNode<T> multi(Function<? super C, ? extends List<? extends ListNode<T>>> init) {
        class MultiListNode extends MultiNode<C, ListNode<T>> implements ListNode<T> {
            public MultiListNode() {
                super(init);
            }
            @Override
            protected MultiNode<C, ListNode<T>> newInstance(Function<? super C, ? extends List<? extends ListNode<T>>> init) {
                return new MultiListNode();
            }
            @Override
            public void add(T entry) {
                all(ListNode::add, entry);
            }
        }
        return new MultiListNode();
    }
    
    static <B,E> Template<B> template(Function<? super InternalComposer<? extends B>, ? extends Consumer<? super E>> factory) {
        class LN implements ListNode<E>, Copyable {
            final InternalComposer<? extends B> ic;
            final Consumer<? super E> consumer;
            public LN(InternalComposer<? extends B> ic) {
                this.ic = ic;
                consumer = factory.apply(ic);
            }
            @Override
            public void add(E entry) {
                consumer.accept(entry);
            }
            @Override
            public Object copyFor(CopyComposer cc) {
                return new LN(cc.getInternal(ic));
            }
            @Override
            public boolean allowReadOnly(Predicate<Object> isLatest) {
                return true; // write-only objects have no readable state
            }
        }
        return Templates.newNode(ic -> new LN(ic));
    }
    
    static <B,E> Template<B> handle(BiConsumer<? super InternalComposer<? extends B>, ? super E> action) {
        class LN implements ListNode<E>, Copyable {
            final InternalComposer<? extends B> ic;
            public LN(InternalComposer<? extends B> ic) {
                this.ic = ic;
            }
            @Override
            public void add(E entry) {
                action.accept(ic, entry);
            }
            @Override
            public Object copyFor(CopyComposer cc) {
                return new LN(cc.getInternal(ic));
            }
            @Override
            public boolean allowReadOnly(Predicate<Object> isLatest) {
                return true; // write-only objects have no readable state
            }
        }
        return Templates.newNode(ic -> new LN(ic));
    }
}
