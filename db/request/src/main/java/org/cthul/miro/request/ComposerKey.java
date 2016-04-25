package org.cthul.miro.request;

import org.cthul.miro.request.template.InternalComposer;
import org.cthul.miro.request.part.ListNode;
import org.cthul.miro.request.part.Copyable;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import org.cthul.miro.request.template.Template;
import org.cthul.miro.request.template.Templates;
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
    
    static QCKey key(Object o) {
        return Key.castDefault(o, QCKey.NIL);
    }
    
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    enum QCKey implements ComposerKey {
        
        ALWAYS,
        
        PHASE,
        
        RESULT,
        
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
                @Override
                public boolean allowReadOnly(Predicate<Object> isLatest) {
                    return true; // write-only objects have no readable state
                }
            }
            return Templates.newNode(ic -> new PL(ic));
        }
    }
}
