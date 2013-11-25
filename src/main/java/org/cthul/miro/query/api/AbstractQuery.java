package org.cthul.miro.query.api;

import org.cthul.miro.query.adapter.QueryAdapter;
import org.cthul.miro.query.parts.QueryPart;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class AbstractQuery<Builder> implements QueryBuilder {
    
    private final Map<String, QueryPart> parts = new HashMap<>();
    private final QueryAdapter<Builder> adapter;
    private final QueryTemplate template;

    public AbstractQuery(QueryAdapter<Builder> adapter, QueryTemplate template) {
        this.adapter = adapter;
        this.template = template;
    }
    
    protected QueryPart newPartFromTemplate(String key) {
        if (template == null) return null;
        QueryTemplatePart tmpl = template.getPartTemplate(key);
        for (String requiredKey: tmpl.getRequiredParts()) {
            put(requiredKey);
        }
        return tmpl.newQueryPart(adapter.getQueryType());
    }
    
    protected QueryPart unknownPart(String key) {
        return newPartFromTemplate(key);
    }
    
    protected synchronized void addPart(String key, QueryPartType<? super Builder> partType, QueryPart part) {
        partType.addPartTo(part, adapter);
        parts.put(key, part);
    }
    
    protected synchronized QueryPart part(String key) {
        QueryPart part = parts.get(key);
        if (part == null) {
            part = unknownPart(key);
            if (part == null) {
                throw new IllegalArgumentException("Unknown key " + key);
            }
//            addPart(key, part);
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
}
