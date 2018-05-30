package org.cthul.miro.sql.composer.model;

import org.cthul.miro.composer.CopyableNodeSet;
import org.cthul.miro.composer.node.StatementPart;
import org.cthul.miro.composer.node.Copyable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.cthul.miro.composer.node.Configurable;
import org.cthul.miro.sql.SelectBuilder;

/**
 *
 */
public abstract class ViewComposerBase implements Copyable<Object>, VirtualView, StatementPart<SelectBuilder> {
    
    private final SqlTemplates owner;
    private final JoinedViews joinedViews;
    private final Map<String, SqlSnippet<? super SelectBuilder>.Part> snippets;
    private final Consumer<Object> dependencyCollector = key -> ViewComposerBase.this.get((String) key);

    public ViewComposerBase(SqlTemplates owner) {
        this.owner = owner;
        this.snippets = new LinkedHashMap<>();
        this.joinedViews = new JoinedViews();
    }

    protected ViewComposerBase(ViewComposerBase source) {
        this.owner = source.owner;
        this.snippets = new LinkedHashMap<>(source.snippets);
        this.joinedViews = source.joinedViews.copy();
    }

    protected SqlTemplates getOwner() {
        return owner;
    }

    protected abstract SqlSnippet<? super SelectBuilder> getSnippet(String key);

    protected void createSnippetPart(String key) {
        SqlSnippet<? super SelectBuilder> snippet = getSnippet(key);
        if (snippet == null) {
            return;
        }
        snippet.requireDependencies(dependencyCollector);
        snippets.put(key, snippet.newPart());
    }

    @Override
    public void addSnippet(String key) {
        int dot = key.indexOf('.');
        if (dot > 0) {
            VirtualView jv = joinedViews.get(key.substring(0, dot));
            if (jv != null) {
                key = key.substring(dot + 1);
                jv.addSnippet(key);
            }
        }
        createSnippetPart(key);
    }

    @Override
    public Configurable get(String key) {
        SqlSnippet<?>.Part snippet = snippets.get(key);
        if (snippet == null) {
            addSnippet(key);
            snippet = snippets.get(key);
        }
        return snippet;
    }

    @Override
    public SqlAttribute getAttribute(String key) {
        SqlAttribute at;
        int dot = key.indexOf('.');
        if (dot > 0) {
            VirtualView jv = joinedViews.get(key.substring(0, dot));
            if (jv == null) {
                return null;
            }
            at = jv.getAttribute(key.substring(dot+1));
            return at.getWithPredix(key.substring(0, dot+1));
        } else {
            at = getOwner().getAttributes().get(key);
            at.getSelectSnippet().requireDependencies(dependencyCollector);
            return at;
        }
    }

    @Override
    public void addTo(SelectBuilder builder) {
        snippets.values().forEach((s) -> {
            s.addTo(builder);
        });
        joinedViews.addPartsTo(builder);
    }

    @Override
    public boolean allowReadOriginal() {
        return true;
    }
    
    protected class JoinedViews extends CopyableNodeSet<String, Object, VirtualView> {
        // ToDo: Every view manages its own joins, 
        // will be a problem if two views want to join in the same tables

        public JoinedViews() {
        }

        public JoinedViews(JoinedViews parent) {
            super(parent);
        }
        
        public JoinedViews copy() {
            return new JoinedViews(this);
        }

        @Override
        public void addPartsTo(Object builder) {
            super.addPartsTo(builder);
        }

        @Override
        protected void newEntry(String key, Object hint) {
            JoinedView jv = getOwner().getJoinedViews().get(key);
            if (jv != null) {
                putNode(key, jv.newVirtualView());
            } else {
                putNullNode(key);
            }
        }

        @Override
        protected Object getInitializationArg() {
            return null;
        }
        
        public VirtualView get(String key) {
            return getValue(key, null);
        }
    }

}
