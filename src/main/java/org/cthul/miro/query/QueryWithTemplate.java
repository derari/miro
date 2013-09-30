package org.cthul.miro.query;

import java.util.List;
import org.cthul.miro.query.QueryTemplate.PartTemplate;

/**
 *
 */
public class QueryWithTemplate extends ParsingQueryBuilder {
    
    private final QueryTemplate template;

    public QueryWithTemplate(QueryTemplate template) {
        this.template = template;
        ensureDependencies(template.getAlwaysRequired());
    }
    
    protected void selectAll() {
        put("*");
    }
    
    protected void selectAllOptional() {
        put("**");
    }
    
    protected void select(String... keys) {
        if (keys == null) {
            selectAll();
        } else {
            putAll(keys);
        }
    }
    
    protected void where(String key, Object... args) {
        put(key, args);
    }
    
    protected void groupBy(String key) {
        put(key);
    }
    
    protected void groupBy(String... keys) {
        putAll(keys);
    }
    
    protected void having(String key, Object... args) {
        put(key, args);
    }
    
    
    protected void orderBy(String key) {
        put(key);
    }
    
    protected void orderBy(String... key) {
        putAll(key);
    }
    
    @Override
    protected void putUnknownKey(String key, String subKey, Object[] args) {
        switch (key) {
            case "*":
                putAll(template.getDefaultFields(), subKey, args);
                break;
            case "**":
                putAll(template.getSelectableFields(), subKey, args);
                break;
            default:
                if (subKey == null) subKey = "";
                addPart(key).put(subKey, args);
        }
    }
    
    private void putAll(String... keys) {
        for (String k: keys) {
            if (k.contains(",")) {
                String[] parts = k.split(",");
                for (String p: parts) {
                    put(p.trim());
                }
            } else {
                put(k);
            }
        }
    }
    
    private void putAll(List<String> keys, String subKey, Object[] args) {
        for (String k: keys) {
            put(k, subKey, args);
        }
    }
    
    private void ensureDependencies(String[] required) {
        for (String key: required) {
            ensurePart(key);
        }
    }

    private void ensureDependencies(List<String> required) {
        for (String key: required) {
            ensurePart(key);
        }
    }
    
    private void ensurePart(String key) {
        if (!parts().containsKey(key)) {
            addPart(key);
        }
    }

    protected QueryPart addPart(String key) {
        return addPartAs(key, key);
    }

    protected QueryPart addPartAs(String key, String alias) {
        PartTemplate pt = template.getPart(key);
        if (pt == null) {
            throw new IllegalArgumentException("Unknown key: " + key);
        }
        ensureDependencies(pt.required);
        QueryPart qp = pt.createPart(alias);
        addPart(qp);
        return qp;
    }
}
