package org.cthul.miro.sql.composer.model;

import org.cthul.miro.sql.composer.node.SelectNodeFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.cthul.miro.db.syntax.QlCode;
import org.cthul.miro.util.ValueKey;
import org.cthul.miro.sql.SelectBuilder;
import org.cthul.miro.sql.composer.Comparison;
import org.cthul.miro.sql.composer.SelectRequest;
import org.cthul.miro.sql.composer.SqlTemplatesBuilder;
import org.cthul.miro.sql.syntax.MiSqlParser;
import org.cthul.miro.util.Key;

/**
 *
 */
public class SqlTemplates 
                implements SqlTemplatesBuilder<SqlTemplates> {

    private final Map<String, SqlAttribute> attributes = new HashMap<>();
    private final Map<String, SqlTable> tables = new HashMap<>();
    private final Map<String, JoinedView> joinedViews = new HashMap<>();
    private final Map<String, SqlSnippet<? super SelectBuilder>> selectSnippets = new ConcurrentHashMap<>();
    private final List<String> keyAttributes = new ArrayList<>();
    private final Key<?> mainKey;

    public SqlTemplates(String name) {
        mainKey = new ValueKey<>(name, true);
    }
    
    public SelectRequest newSelectComposer() {
        return new SelectNodeFactory(this).newComposer();
    }

    public Map<String, SqlAttribute> getAttributes() {
        return attributes;
    }

    protected Map<String, SqlTable> getTables() {
        return tables;
    }

//    public Map<String, JoinedView> getJoinedViews() {
//        return joinedViews;
//    }

    public void collectJoinedViews(BiConsumer<String, JoinedView> bag) {
        joinedViews.entrySet().forEach(e -> bag.accept(e.getKey(), e.getValue()));
    }
    
    private Key<?> getMainKey() {
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
        return (SqlTemplates) this;
    }

    @Override
    public SqlTemplates key(String attributeKey) {
        keyAttributes.add(attributeKey);
        return this;
    }

    @Override
    public SqlTemplates table(SqlTable table) {
        tables.put(table.getKey(), table);
        return (SqlTemplates) this;
    }

    @Override
    public SqlTemplates join(JoinedView view) {
        joinedViews.put(view.getPrefix(), view);
        return (SqlTemplates) this;
    }

//    @Override
//    public <V extends Template<? super SqlFilterableClause>> SqlTemplates where(Key<? super V> key, V filter) {
////        templates.put(key, filter);
//        return (SqlTemplates) this;
//    }

    @Override
    public SqlTemplates selectSnippet(SqlSnippet<? super SelectBuilder> snippet) {
        selectSnippets.put(snippet.getKey(), snippet);
        return (SqlTemplates) this;
    }
    
    public JoinedView joinedAs(String prefix, Function<List<SqlAttribute>, List<Object>> foreignKeys) {
        return new JoinedView() {
            Function<List<SqlAttribute>, List<Object>> keySupplier = foreignKeys;
            QlCode condition = null;
            @Override
            public String getPrefix() {
                return prefix;
            }
            @Override
            public VirtualView newVirtualView() {
                if (condition == null) condition = initCondition();
                return new JoinedVirtualView(prefix, condition, SqlTemplates.this);
            }
            @Override
            public String toString() { 
                return getMainKey() + " AS " + prefix;
            }
            @Override
            public void collectJoinedViews(BiConsumer<String, JoinedView> bag) {
                SqlTemplates.this.collectJoinedViews(bag);
            }
            private QlCode initCondition() {
                QlCode.Builder code = QlCode.build();
                List<SqlAttribute> keys = new ArrayList<>();
                keyAttributes.forEach(k -> keys.add(getAttributes().get(k)));
                List<Object> foreigns = keySupplier.apply(keys);
                for (int i = 0; i < foreigns.size(); i++) {
                    if (i > 0) code.append(" AND ");
                    SqlAttribute key = keys.get(i);
                    code.append(key.expression()).append(" ");
                    Comparison.appendTo(foreigns.get(i), code, (v, q) -> {
                        ((QlCode) v).appendTo(q);
                    });
                }
                keySupplier = null;
                return code;
            }
        };
    }
    
    public JoinedView joinedAs(String prefix, String condition) {
        return joinedAs(prefix, MiSqlParser.parseCode(condition));
    }
    
    private JoinedView joinedAs(String prefix, QlCode condition) {
        return new JoinedView() {
            @Override
            public String getPrefix() { 
                return prefix;
            }
            @Override
            public VirtualView newVirtualView() {
                return new JoinedVirtualView(prefix, condition, SqlTemplates.this);
            }
            @Override
            public String toString() { 
                return getMainKey()+ " AS " + prefix;
            }
            @Override
            public void collectJoinedViews(BiConsumer<String, JoinedView> bag) {
                SqlTemplates.this.collectJoinedViews(bag);
            }
        };
    }
}
