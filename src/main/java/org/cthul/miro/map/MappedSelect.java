package org.cthul.miro.map;

import org.cthul.miro.query.api.QueryType;

public class MappedSelect<Entity> extends AbstractMappedQuery<Entity> {

    public MappedSelect(QueryType<?> type, MappedTemplateProvider<Entity> templateProvider, String[] select) {
        super(type, templateProvider);
        putAll(select);
    }
    
    public MappedSelect where(String key, Object... args) {
        put(key, args);
        return this;
    }
    
}
