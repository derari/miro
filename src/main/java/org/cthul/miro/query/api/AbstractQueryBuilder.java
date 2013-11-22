package org.cthul.miro.query.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cthul.miro.query.syntax.SqlBuilder;

/**
 *
 */
public class AbstractQueryBuilder implements QueryBuilder {
    
    private final Map<String, QueryPart> parts = new HashMap<>();
    private final SqlBuilder sqlBuilder;
    private final QueryTemplate template;

    public AbstractQueryBuilder(SqlBuilder sqlBuilder, QueryTemplate template) {
        this.sqlBuilder = sqlBuilder;
        this.template = template;
    }
    
    protected QueryPart newPartFromTemplate(String key) {
        if (template == null) return null;
        QueryPartTemplate tmpl = template.getPartTemplate(key);
        for (String requiredKey: tmpl.getRequiredParts()) {
            put(requiredKey);
        }
        return tmpl.newQueryPart(sqlBuilder.getQueryType());
    }
    
    protected QueryPart unknownPart(String key) {
        return newPartFromTemplate(key);
    }
    
    protected synchronized void addPart(String key, QueryPart part) {
        sqlBuilder.addPart(part);
        parts.put(key, part);
    }
    
    protected synchronized QueryPart part(String key) {
        QueryPart part = parts.get(key);
        if (part == null) {
            part = unknownPart(key);
            if (part == null) {
                throw new IllegalArgumentException("Unknown key " + key);
            }
            addPart(key, part);
        }
        return part;
    }
    
    @Override
    public void put(String key) {
        part(key);
    }

    @Override
    public void put(String key, Object... args) {
        put(key, "", args);
    }

    @Override
    public void put(String key, String subkey, Object... args) {
        part(key).put(subkey, args);
    }
    
    protected String getQueryString() {
        return sqlBuilder.getQueryString();
    }
    
    protected List<Object> getArguments() {
        return sqlBuilder.getArguments();
    }
}
