package org.cthul.miro.composer;

import org.cthul.miro.util.Key;

/**
 *
 * @param <Value>
 */
public interface QueryComposerKey<Value> extends Key<Value> {
    
    static final QueryComposerKey<PhaseListener> PHASE = QCKey.PHASE;
    
    static QCKey key(Object o) {
        return Key.castDefault(o, QCKey.NIL);
    }
    
    enum QCKey implements QueryComposerKey {
        PHASE,
        
        NIL;
    }
    
    enum Phase {
        COMPOSE,
        BUILD;
    }
    
    interface PhaseListener extends QueryParts.ComposableNode<PhaseListener> {
    
        void enter(Phase phase);
        
        @Override
        default PhaseListener and(PhaseListener other) {
            return p -> {
                this.enter(p);
                other.enter(p);
            };
        }
    }
}
