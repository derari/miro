package org.cthul.miro.composer;

import org.cthul.miro.util.Key;

/**
 *
 * @param <Value>
 */
public interface ComposerKey<Value> extends Key<Value> {
    
    public static final ComposerKey<Void> ALWAYS = QCKey.ALWAYS;
    public static final ComposerKey<PhaseListener> PHASE = QCKey.PHASE;
    public static final ComposerKey<ResultAttributes> RESULT = QCKey.RESULT;
    public static final ComposerKey<Void> FETCH_KEYS = QCKey.FETCH_KEYS;
    
    static QCKey key(Object o) {
        return Key.castDefault(o, QCKey.NIL);
    }
    
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    enum QCKey implements ComposerKey {
        
        ALWAYS,
        
        PHASE,
        
        RESULT,
        
        FETCH_KEYS,
        
        NIL;
    }
    
    enum Phase {
        COMPOSE,
        BUILD;
    }
    
    interface PhaseListener extends ComposerParts.ComposableNode<PhaseListener> {
    
        void enter(Phase phase);
        
        @Override
        default PhaseListener and(PhaseListener other) {
            return new MultiPhaseListener(this, other);
        }
    }
    
    class MultiPhaseListener extends ComposerParts.MultiNode<PhaseListener>
                             implements PhaseListener {
        public MultiPhaseListener(PhaseListener... nodes) {
            super(nodes);
        }
        @Override
        public void enter(Phase phase) {
            all(PhaseListener::enter, phase);
        }
    }
    
    interface ResultAttributes extends ComposerParts.ComposableNode<ResultAttributes> {
        
        void add(String attribute);

        @Override
        public default ResultAttributes and(ResultAttributes other) {
            return new MultiResultAttributes(other);
        }
    }
    
    class MultiResultAttributes extends ComposerParts.MultiNode<ResultAttributes>
                             implements ResultAttributes {
        public MultiResultAttributes(ResultAttributes... nodes) {
            super(nodes);
        }

        @Override
        public void add(String attribute) {
            all(ResultAttributes::add, attribute);
        }
    }
}
