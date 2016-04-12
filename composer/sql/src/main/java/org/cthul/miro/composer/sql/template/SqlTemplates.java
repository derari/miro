package org.cthul.miro.composer.sql.template;

import org.cthul.miro.composer.sql.SqlAttribute;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.cthul.miro.composer.Configurable;
import org.cthul.miro.composer.MapNode;
import org.cthul.miro.composer.impl.ValueKey;
import org.cthul.miro.composer.sql.SqlSnippet;
import org.cthul.miro.composer.sql.SqlTable;
import org.cthul.miro.composer.sql.SqlTemplatesBuilder;
import org.cthul.miro.composer.template.Template;
import org.cthul.miro.composer.template.TemplateLayer;
import org.cthul.miro.composer.template.TemplateLayerStack;
import org.cthul.miro.db.sql.SelectBuilder;
import org.cthul.miro.db.sql.SqlFilterableClause;
import org.cthul.miro.db.sql.syntax.MiSqlParser;
import org.cthul.miro.util.Key;

/**
 *
 */
public class SqlTemplates implements SqlTemplatesBuilder<SqlTemplates> {

    private final Map<String, SqlAttribute> attributes = new HashMap<>();
    private final Map<String, SqlTable> tables = new HashMap<>();
    private final Map<String, JoinedView> joinedViews = new HashMap<>();
    private final Map<String, SqlSnippet<? super SelectBuilder>> selectSnippets = new ConcurrentHashMap<>();
    private final Key<MapNode<String, Configurable>> mainKey;

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
        TemplateLayerStack stack = new TemplateLayerStack();
        joinedViews.values().forEach(jv -> stack.push(jv.newSelectLayer()));
        stack.push(generalLayer());
        stack.push(simpleSelectLayer());
        return stack;
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

    public Key<MapNode<String, Configurable>> getMainKey() {
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
    
    public JoinedView joinAs(String prefix, String condition) {
        return new JoinedView() {
            @Override
            public String getPrefix() { 
                return prefix;
            }
            @Override
            public Key<MapNode<String, Configurable>> getSnippetKey() {
                return getMainKey();
            }
            @Override
            public TemplateLayer<? super SelectBuilder> newSelectLayer() {
                return new JoinedLayer<>(SqlTemplates.this, getSnippetKey(), prefix, condition);
            }
            @Override
            public String toString() { 
                return getSnippetKey() + " AS " + prefix;
            }
        };
    }
}
