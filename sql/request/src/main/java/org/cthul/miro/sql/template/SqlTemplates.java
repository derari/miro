package org.cthul.miro.sql.template;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.cthul.miro.request.impl.ValueKey;
import org.cthul.miro.sql.template.JoinedView.Layers;
import org.cthul.miro.request.template.Template;
import org.cthul.miro.request.template.TemplateLayer;
import org.cthul.miro.request.template.TemplateLayerStack;
import org.cthul.miro.sql.SelectBuilder;
import org.cthul.miro.sql.SqlFilterableClause;
import org.cthul.miro.sql.syntax.MiSqlParser;
import org.cthul.miro.util.Key;

/**
 *
 */
public class SqlTemplates implements SqlTemplatesBuilder<SqlTemplates> {

    private final Map<String, SqlAttribute> attributes = new HashMap<>();
    private final Map<String, SqlTable> tables = new HashMap<>();
    private final Map<String, JoinedView> joinedViews = new HashMap<>();
    private final Map<String, SqlSnippet<? super SelectBuilder>> selectSnippets = new ConcurrentHashMap<>();
    private final Key<ViewComposer> mainKey;

    public SqlTemplates(String name) {
        mainKey = new ValueKey<>(name, true);
    }
    
    protected TemplateLayer<SqlFilterableClause> generalLayer() {
        return new GeneralSqlLayer<>(this);
    }
    
    protected TemplateLayer<SelectBuilder> simpleSelectLayer() {
        return new SelectLayer<>(this);
    }
    
    public TemplateLayer<SelectBuilder> getSelectLayer() {
        Set<Object> keys = new HashSet<>();
        TemplateLayerStack stack = new TemplateLayerStack();
        Layers<SelectBuilder> bag = (k, t) -> {
            if (!keys.add(k)) return false;
            stack.push(t);
            return true;
        };
        collectJoinedSelectTemplateLayers(bag);
        stack.push(generalLayer());
        stack.push(simpleSelectLayer());
        return stack;
    }
    
    protected void collectJoinedSelectTemplateLayers(Layers<SelectBuilder> bag) {
        joinedViews.values().forEach(jv -> jv.collectSelectTemplateLayers(bag));
    }

    protected Map<String, SqlAttribute> getAttributes() {
        return attributes;
    }

    protected Map<String, SqlTable> getTables() {
        return tables;
    }

    public Map<String, JoinedView> getJoinedViews() {
        return joinedViews;
    }

    public Key<ViewComposer> getMainKey() {
        return mainKey;
    }
    
    public SqlSnippet<? super SelectBuilder> getSelectSnippet(String key) {
        return selectSnippets.computeIfAbsent(key, this::createSelectSnippet);
    }
    
    public SqlSnippet<? super SelectBuilder> createSelectSnippet(String key) {
        SqlAttribute at = getAttributes().get(key);
        if (at != null) {
            return at.getSelectSnippet();
        }
        SqlTable tb = getTables().get(key);
        if (tb != null) {
            return tb.getSnippet();
        }
        key = key.trim();
        int space = key.indexOf(' ');
        String keyword = key.substring(0, space < 0 ? 0 : space);
        if (MiSqlParser.isKeyword(keyword)) {
            MiSqlParser.SelectStmt stmt = MiSqlParser.parsePartialSelect(key);
            return SqlSnippet.select(key, stmt);
        }
        return null;
    }

    @Override
    public SqlTemplates attribute(SqlAttribute attribute) {
        attributes.put(attribute.getKey(), attribute);
        return this;
    }

    @Override
    public SqlTemplates table(SqlTable table) {
        tables.put(table.getKey(), table);
        return this;
    }

    @Override
    public SqlTemplates join(JoinedView view) {
        joinedViews.put(view.getPrefix(), view);
        return this;
    }

    @Override
    public <V extends Template<? super SqlFilterableClause>> SqlTemplates where(Key<? super V> key, V filter) {
//        templates.put(key, filter);
        return this;
    }
    
    public JoinedView joinedAs(String prefix, String condition) {
        return new JoinedView() {
            @Override
            public String getPrefix() { 
                return prefix;
            }
            @Override
            public Key<ViewComposer> getViewKey() {
                return getMainKey();
            }
            @Override
            public void collectSelectTemplateLayers(JoinedView.Layers<SelectBuilder> bag) {
                JoinedLayer<SelectBuilder> jl = new JoinedLayer<>(SqlTemplates.this, getViewKey(), prefix, condition);
                if (bag.add(getViewKey(), jl)) {
                    collectJoinedSelectTemplateLayers(bag);
                }
            }
            @Override
            public String toString() { 
                return getViewKey() + " AS " + prefix;
            }
        };
    }
}
