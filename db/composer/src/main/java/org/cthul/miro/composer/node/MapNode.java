package org.cthul.miro.composer.node;

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
}
