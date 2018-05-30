package org.cthul.miro.composer.node;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 * @param <Entry>
 */
public interface ListNode<Entry> extends BatchNode<Entry> {
    
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
    public default void set(Collection<? extends Entry> values) {
        addAll(values);
    }

    @Override
    public default void set(Entry... values) {
        addAll(values);
    }

//    @Override
//    public default Object and(ListNode<Entry> other) {
//        class Multi extends Templates.MultiNode<ListNode<Entry>>
//                    implements ListNode<Entry> {
//            public Multi() {
//                super(ListNode.this, other);
//            }
//            @Override
//            public void add(Entry entry) {
//                all(ListNode::add, entry);
//            }
//        }
//        return new Multi();
//    }
    
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
}
