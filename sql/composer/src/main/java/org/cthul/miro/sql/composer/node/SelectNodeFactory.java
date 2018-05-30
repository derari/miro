package org.cthul.miro.sql.composer.node;

import org.cthul.miro.composer.node.CopyInitializable;
import org.cthul.miro.composer.ComposerState;
import org.cthul.miro.composer.node.Initializable;
import org.cthul.miro.composer.node.ListNode;
import org.cthul.miro.sql.SelectBuilder;
import org.cthul.miro.sql.composer.AttributeFilter;
import org.cthul.miro.sql.composer.SelectComposer;
import org.cthul.miro.sql.composer.SelectRequest;
import org.cthul.miro.sql.composer.model.SqlSnippet;
import org.cthul.miro.sql.composer.model.SqlTemplates;
import org.cthul.miro.sql.composer.model.ViewComposerBase;
import org.cthul.miro.sql.composer.model.VirtualView;
import org.cthul.miro.sql.composer.node.AttributeFilterPart;

/**
 *
 */
public class SelectNodeFactory implements SelectComposer {
    
    private final SqlTemplates owner;

    public SelectNodeFactory(SqlTemplates owner) {
        this.owner = owner;
    }
    
    public SelectRequest newComposer() {
        return ComposerState.builder()
                .setFactory(this)
                .create(SelectRequest.class);
    }

    protected SqlTemplates getOwner() {
        return owner;
    }

    @Override
    public VirtualView getMainView() {
        return new SelectView();
    }

    @Override
    public ListNode<String> getSelectedAttributes() {
        class SelectAttributes extends CopyInitializable<SelectComposer> implements ListNode<String> {
            VirtualView view;
            @Override
            public void initialize(SelectComposer composer) {
                view = composer.getMainView();
            }
            @Override
            protected Initializable<SelectComposer> copyInstance() {
                return new SelectAttributes();
            }
            @Override
            public void add(String entry) {
                view.get(entry);
            }
        }
        return new SelectAttributes();
    }

    @Override
    public AttributeFilter getAttributeFilter() {
        return new AttributeFilterPart(owner);
    }
    
    
    protected class SelectView extends ViewComposerBase {

        public SelectView() {
            super(SelectNodeFactory.this.getOwner());
        }

        public SelectView(ViewComposerBase source) {
            super(source);
        }

        @Override
        protected SqlSnippet<? super SelectBuilder> getSnippet(String key) {
            return getOwner().getSelectSnippet(key);
        }

        @Override
        public Object copy(Object composer) {
            return new SelectView(this);
        }
    }
    
    
    
//    protected static class Impl implements SelectComposerDelegator, Initializable<SelectComposer> {
//        
//        SelectComposer actual = null;
//
//        @Override
//        public SelectComposer getSelectComposerDelegate() {
//            return actual;
//        }
//
//        @Override
//        public void initialize(SelectComposer composer) {
//            actual = composer;
//        }
//    }
}
