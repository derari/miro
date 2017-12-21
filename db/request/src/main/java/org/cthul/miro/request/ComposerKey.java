package org.cthul.miro.request;

import org.cthul.miro.request.template.InternalComposer;
import org.cthul.miro.request.part.Copyable;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import org.cthul.miro.request.template.Templates;
import org.cthul.miro.request.template.Templates.ComposableTemplate;
import org.cthul.miro.util.Key;

/**
 *
 * @param <Value>
 */
public interface ComposerKey<Value> extends Key<Value> {
    
    /** This key is always required. */
    final ComposerKey<?> ALWAYS = CKey.ALWAYS;
    
    /** Phase listeners are notified about phases in the query building process. */
    final ComposerKey<PhaseListener> PHASE = CKey.PHASE;
    
    static CKey key(Object o) {
        return Key.castDefault(o, CKey.NIL);
    }
    
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    enum CKey implements ComposerKey {
        
        ALWAYS,
        
        PHASE,
        
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
        
        static <B> ComposableTemplate<B> handle(BiConsumer<? super InternalComposer<? extends B>, ? super Phase> action) {
            class PL implements PhaseListener, Copyable {
                final InternalComposer<? extends B> ic;
                public PL(InternalComposer<? extends B> ic) {
                    this.ic = ic;
                }
                @Override
                public void enter(Phase entry) {
                    action.accept(ic, entry);
                }
                @Override
                public Object copyFor(CopyComposer cc) {
                    return new PL(cc.getInternal(ic));
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
