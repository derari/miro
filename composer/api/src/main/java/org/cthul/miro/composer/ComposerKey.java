package org.cthul.miro.composer;

import java.util.function.BiConsumer;
import org.cthul.miro.composer.template.Template;
import org.cthul.miro.composer.template.Templates;
import org.cthul.miro.util.Key;

/**
 *
 * @param <Value>
 */
public interface ComposerKey<Value> extends Key<Value> {
    
    /** This key is always required. */
    final ComposerKey<?> ALWAYS = QCKey.ALWAYS;
    
    /** Phase listeners are notified about phases in the query building process. */
    final ComposerKey<PhaseListener> PHASE = QCKey.PHASE;
    
    /** Allows to add attributes to the result. */
    final ComposerKey<ListNode<String>> RESULT = QCKey.RESULT;
    
    /** Requires that all key attributes are in the result */
    final ComposerKey<?> FETCH_KEYS = QCKey.FETCH_KEYS;
    
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
    
    interface PhaseListener extends Templates.ComposableNode<PhaseListener> {
    
        void enter(Phase phase);
        
        @Override
        default PhaseListener and(PhaseListener other) {
            class Multi extends Templates.MultiNode<PhaseListener>
                        implements PhaseListener {
                public Multi() {
                    super(PhaseListener.this, other);
                }
                @Override
                public void enter(Phase phase) {
                    all(PhaseListener::enter, phase);
                }
            }
            return new Multi();
        }
        
        static <B> Template<B> handle(BiConsumer<? super InternalComposer<? extends B>, ? super Phase> action) {
            class PL implements PhaseListener, Copyable<B> {
                final InternalComposer<? extends B> ic;
                public PL(InternalComposer<? extends B> ic) {
                    this.ic = ic;
                }
                @Override
                public void enter(Phase entry) {
                    action.accept(ic, entry);
                }
                @Override
                public Object copyFor(InternalComposer<B> ic) {
                    return new PL(ic);
                }
            }
            return Templates.newNode(ic -> new PL(ic));
        }
    }
}
