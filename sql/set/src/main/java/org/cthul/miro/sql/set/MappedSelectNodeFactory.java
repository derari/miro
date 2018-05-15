package org.cthul.miro.sql.set;

import org.cthul.miro.map.MappedQuery;
import org.cthul.miro.map.MappedQueryComposer;
import org.cthul.miro.map.PropertyFilterComposer;
import org.cthul.miro.request.ComposerState;
import org.cthul.miro.request.ComposerState.Behavior;
import org.cthul.miro.request.part.ListNode;
import org.cthul.miro.sql.template.AttributeFilter;
import org.cthul.miro.sql.template.SelectComposer;

/**
 *
 */
public class MappedSelectNodeFactory<Entity> /* implements MappedSelectComposer, NodeFactory*/ {
    
    public static <Entity> MappedSelectRequest<Entity> newComposer(MappedQueryComposer<Entity> mappedQueryComposer, SelectComposer selectComposer) {
        return ComposerState.builder()
                .setImpl(new Impl<>())
                .addInterfaces(MappedSelectRequest.class, mappedQueryComposer, selectComposer)
                .putAdapted("getMappedQueryComposerDelegate", mappedQueryComposer, (MappedQuery q) -> q.getMapping())
                .putAdapted("getSelectComposerDelegate", selectComposer, (MappedQuery q) -> q.getStatement())
                .create();
    }
//
//    private final MappedQueryComposer.Internal mappedQueryComposerPrototype;
//    private final SelectComposer selectComposerPrototype;
//    private final MappedSelectRequest<Entity> prototype;
//
////
////    @Override
////    public Internal getMappedQueryComposerInternalDelegate() {
////        throw new UnsupportedOperationException();
////    }
////
////    @Override
////    public MappedQueryComposer getMappedQueryComposerDelegate() {
////        return null;
////    }
////
////    @Override
////    public SelectComposer getSelectComposerDelegate() {
////        return null;
////    }
//
//    @SuppressWarnings("LeakingThisInConstructor")
//    public MappedSelectNodeFactory(MappedQueryComposer mappedQueryComposer, SelectComposer selectComposer) {
//        this.mappedQueryComposerPrototype = ComposerState.adapt((MappedQueryComposer.Internal) mappedQueryComposer, (MappedQuery q) -> q.getMapping());
//        this.selectComposerPrototype = ComposerState.adapt(selectComposer, (MappedQuery q) -> q.getStatement());
//        this.prototype = ComposerState.newComposer(new Impl(), this, MappedSelectRequest.class);
//    }
//    
//    public MappedSelectRequest<Entity> newComposer() {
//        return ComposerState.copy(prototype);
//    }
//
//    @Override
//    public MappedQueryComposer.Internal getMappedQueryComposerInternalDelegate() {
//        return ComposerState.copy(mappedQueryComposerPrototype);
//    }
//
//    @Override
//    public SelectComposer getSelectComposerDelegate() {
//        return ComposerState.copy(selectComposerPrototype);
//    }
//
    protected static class Impl<Entity> implements MappedSelectComposer.Delegator<Entity>, MappedSelectComposer.Internal<Entity>, Behavior<MappedSelectComposer.Delegator<Entity>> {
        
        private MappedSelectComposer.Delegator<Entity> actual = null;

        @Override
        public Behavior<MappedQueryComposer.Delegator> copy() {
            return new Impl();
        }

        @Override
        public void initialize(MappedSelectComposer.Delegator<Entity> composer) {
            actual = composer;
        }

        @Override
        public MappedQueryComposer<Entity> getMappedQueryComposerDelegate() {
            return actual.getMappedQueryComposerDelegate();
        }

        @Override
        public SelectComposer getSelectComposerDelegate() {
            return actual.getSelectComposerDelegate();
        }

        @Override
        public PropertyFilterComposer getPropertyFilterComposerDelegate() {
            return getMappedQueryComposerDelegate();
        }

        @Override
        public ListNode<String> getSelectedAttributes() {
            return MappedSelectComposer.Delegator.super.getSelectedAttributes();
        }

        @Override
        public AttributeFilter getAttributeFilter() {
            return MappedSelectComposer.Delegator.super.getAttributeFilter();
        }
    }
}
