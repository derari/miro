package org.cthul.miro.dsl;

import java.util.List;
import org.cthul.miro.MiConnection;
import org.cthul.miro.dsl.QueryTemplate.PartTemplate;
import org.cthul.miro.map.Mapping;

/**
 *
 */
public class QueryWithTemplate<Entity> extends QueryBuilder<Entity> {
    
    private final QueryTemplate<Entity> template;

    public QueryWithTemplate(MiConnection cnn, Mapping<Entity> mapping, QueryTemplate<Entity> template) {
        super(cnn, mapping);
        this.template = template;
        ensureDependencies(template.getAlwaysRequired());
    }
    
    protected void select_key(String... keys) {
        select_keys(keys);
    }
    
    protected void select_keys(String... keys) {
        if (keys == null) {
            put("*");
        } else {
            for (String k: keys) {
                put(k);
            }
        }
    }
    
    protected void where_key(String key, Object... args) {
        put(key, args);
    }
    
    protected void groupBy_key(String key) {
        put(key);
    }
    
    protected void groupBy_key(String... key) {
        groupBy_keys(key);
    }
    
    protected void groupBy_keys(String... key) {
        for (String k: key) {
            put(k);
        }
    }
    
    protected void having_key(String key, Object... args) {
        put(key, args);
    }
    
    private void putAll(List<String> keys, String subKey, Object[] args) {
        for (String k: keys) {
            put(k, subKey, args);
        }
    }
    
    protected void orderBy_key(String key) {
        put(key);
    }
    
    protected void orderBy_key(String... key) {
        groupBy_keys(key);
    }
    
    protected void orderBy_keys(String... key) {
        for (String k: key) {
            put(k);
        }
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
        if (!parts.containsKey(key)) {
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
