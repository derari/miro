package org.cthul.miro.graph;

import org.cthul.miro.entity.FactoryBuilder;
import org.cthul.miro.util.XFunction;

/**
 *
 * @param <Node>
 */
public interface SelectorBuilder<Node> extends FactoryBuilder<Node> {
    
    <N extends Node> SelectorBuilder<N> set(NodeSelector<N> factory);
    
    <N extends Node> SelectorBuilder<N> setFactory(XFunction<Object[], N, ?> factory);
    
    default <N extends Node> SelectorBuilder<N> setNamedFactory(XFunction<Object[], N, ?> factory) {
        SelectorBuilder<N> fb = setFactory(factory);
        fb.addName(factory);
        return fb;
    }
}
