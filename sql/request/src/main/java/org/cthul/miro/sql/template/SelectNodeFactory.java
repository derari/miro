package org.cthul.miro.sql.template;

import java.util.*;
import org.cthul.miro.request.*;
import org.cthul.miro.request.part.Configurable;
import org.cthul.miro.request.part.ListNode;
import org.cthul.miro.sql.SelectBuilder;
import org.cthul.miro.sql.template.impl.AttributeFilterPart;
import org.cthul.miro.util.Key;

/**
 *
 */
public class SelectNodeFactory implements SelectComposer {
    
    private final SqlTemplates owner;

    public SelectNodeFactory(SqlTemplates owner) {
        this.owner = owner;
    }
    
    public SelectComposer newComposer() {
        return ComposerState.fromFactory(this);
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
            super();
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
    
    protected abstract class ViewComposerBase implements Copyable2<Object>, VirtualView, StatementPart<SelectBuilder> {

        final Map<String, SqlSnippet<? super SelectBuilder>.Part> snippets;
        final Composer dependencyComposer = new Composer() {
            @Override
            public boolean include(Object key) {
                return ViewComposerBase.this.get((String) key) != null;
            }
            @Override
            public <V> V get(Key<V> key) {
                throw new UnsupportedOperationException();
            }
        };

        public ViewComposerBase() {
            this.snippets = new LinkedHashMap<>();
        }
        
        protected ViewComposerBase(ViewComposerBase source) {
            this.snippets = new LinkedHashMap<>(source.snippets);
        }

        protected abstract SqlSnippet<? super SelectBuilder> getSnippet(String key);
        
        protected void createSnippetPart(String key) {
            SqlSnippet<? super SelectBuilder> snippet = getSnippet(key);
            if (snippet == null) return;
            snippet.requireDependencies(dependencyComposer);
            snippets.put(key, snippet.newPart());
        }

        @Override
        public void addSnippet(String key) {
            int dot = key.indexOf('.');
            if (dot > 0) {
                JoinedView jv = getOwner().getJoinedViews().get(key.substring(0, dot));
                if (jv != null) {
                    key = key.substring(dot+1);
//                    Configurable c = ic.node(jv.getViewKey()).get(name);
//                    ic.addNode(key, c);
                    throw new UnsupportedOperationException();
                }
            }
            createSnippetPart(key);
        }

        @Override
        public Configurable get(String key) {
            SqlSnippet<?>.Part snippet  = snippets.get(key);
            if (snippet == null) {
                addSnippet(key);
                snippet  = snippets.get(key);
            }
            return snippet;
        }

        @Override
        public SqlAttribute getAttribute(String key) {
            SqlAttribute at;
            int dot = key.indexOf('.');
            if (dot > 0) {
                JoinedView jv = getOwner().getJoinedViews().get(key.substring(0, dot));
                if (jv == null) return null;
//                at = ic.node(jv.getViewKey()).getAttribute(key.substring(dot+1));
//                return at.getWithPredix(key.substring(0, dot+1));
                throw new UnsupportedOperationException();
            } else {
                at = getOwner().getAttributes().get(key);
                at.getSelectSnippet().requireDependencies(dependencyComposer);
                return at;
            }
        }

        @Override
        public void addTo(SelectBuilder builder) {
            snippets.values().forEach(s -> {
                s.addTo(builder);
            });
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
