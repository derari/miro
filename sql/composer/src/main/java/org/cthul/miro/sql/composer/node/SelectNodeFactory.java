package org.cthul.miro.sql.composer.node;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import org.cthul.miro.composer.node.CopyInitializable;
import org.cthul.miro.composer.ComposerState;
import org.cthul.miro.composer.ComposerState.Behavior;
import org.cthul.miro.composer.CopyableNodeSet;
import org.cthul.miro.composer.node.*;
import org.cthul.miro.sql.SelectBuilder;
import org.cthul.miro.sql.composer.*;
import org.cthul.miro.sql.composer.model.*;

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
                .setImpl(new Impl())
                .setFactory(this)
                .create(SelectRequest.class);
    }

    protected SqlTemplates getOwner() {
        return owner;
    }

    @Override
    public VirtualView getMainView() {
        throw new UnsupportedOperationException("Impl");
    }

    @Override
    public MapNode<String, VirtualView> getViews() {
        return new ViewMap();
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
        protected Initializable<SqlDqmlComposer> copyInstance() {
            return new SelectView(this);
        }
    }
    
    protected class ViewMap extends CopyableNodeSet<String, Void, VirtualView> 
            implements MapNode<String, VirtualView>, StatementPart<SelectBuilder>,
                        Initializable<SqlDqmlComposer>, Copyable<SqlDqmlComposer> {
        
        private final Map<String, JoinedView> views;
        private SqlDqmlComposer cmp;

        public ViewMap() {
            this.views = new HashMap<>();
            owner.collectJoinedViews(initializeViewsBag());
        }

        public ViewMap(ViewMap source) {
            super(source);
            this.views = source.views;
        }

        @Override
        public void initialize(SqlDqmlComposer composer) {
            this.cmp = composer;
            putNode("", new SelectView());
        }

        @Override
        public Object copy(SqlDqmlComposer composer) {
            ViewMap copy = new ViewMap(this);
            copy.cmp = composer;
            return copy;
        }

        @Override
        protected void newEntry(String key, Void hint) {
            JoinedView jv = views.get(key);
            if (jv != null) {
                putNode(key, jv.newVirtualView());
            } else {
                putNullNode(key);
            }
        }

        @Override
        protected Object getInitializationArg() {
            return cmp;
        }

        @Override
        public VirtualView get(String key) {
            return getValue(key, null);
        }
        
        private BiConsumer<String, JoinedView> initializeViewsBag() {
            return new BiConsumer<String, JoinedView>() {
                @Override
                public void accept(String key, JoinedView view) {
                    if (views.putIfAbsent(key, view) == null) {
                        view.collectJoinedViews(this);
                    }
                }
            };
        }

        @Override
        public void addTo(SelectBuilder builder) {
            addPartsTo(builder);
        }
    }
    
    protected static class Impl implements Behavior<SelectComposer> {
        SelectComposer actual;

        @Override
        public Object copy() {
            return new Impl();
        }

        @Override
        public void initialize(SelectComposer composer) {
            this.actual = composer;
        }
        
        public VirtualView getMainView() {
            return actual.getViews().get("");
        }
    }
}
