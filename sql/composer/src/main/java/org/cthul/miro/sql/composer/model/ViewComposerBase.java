package org.cthul.miro.sql.composer.model;

import org.cthul.miro.composer.node.StatementPart;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.cthul.miro.composer.node.*;
import org.cthul.miro.sql.SelectBuilder;
import org.cthul.miro.sql.composer.SqlDqmlComposer;

/**
 *
 */
public abstract class ViewComposerBase extends CopyInitializable<SqlDqmlComposer> implements VirtualView, StatementPart<SelectBuilder> {
    
    private final SqlTemplates owner;
    private final Map<String, SqlSnippet<? super SelectBuilder>.Part> snippets;
    private final Consumer<Object> dependencyCollector = key -> ViewComposerBase.this.get((String) key);
    private MapNode<String, VirtualView> joinedViews;

    public ViewComposerBase(SqlTemplates owner) {
        this.owner = owner;
        this.snippets = new LinkedHashMap<>();
    }

    protected ViewComposerBase(ViewComposerBase source) {
        this.owner = source.owner;
        this.snippets = new LinkedHashMap<>(source.snippets);
    }

    @Override
    public void initialize(SqlDqmlComposer composer) {
        joinedViews = composer.getViews();
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
    }

    @Override
    public boolean allowReadOriginal() {
        return true;
    }
}
